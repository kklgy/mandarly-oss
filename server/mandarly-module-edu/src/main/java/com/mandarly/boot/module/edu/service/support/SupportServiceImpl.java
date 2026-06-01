package com.mandarly.boot.module.edu.service.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportContactPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportContactSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportFaqPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportFaqSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportInquiryPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportInquiryStatsRespVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportTopUnmatchedRespVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportAskReqVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportAskRespVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportBootstrapRespVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportContactRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.support.FaqDO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportContactDO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportInquiryLogDO;
import com.mandarly.boot.module.edu.dal.mysql.support.FaqMapper;
import com.mandarly.boot.module.edu.dal.mysql.support.SupportContactMapper;
import com.mandarly.boot.module.edu.dal.mysql.support.SupportInquiryLogMapper;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.SUPPORT_RATE_LIMIT;

@Service
@Slf4j
public class SupportServiceImpl implements SupportService {

    private static final String DEFAULT_MARKET = "DEFAULT";
    private static final String DEFAULT_LOCALE = "en";
    private static final String CONFIG_KEY_THRESHOLD = "support.faq_match_threshold";
    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("0.300");
    private static final int RATE_LIMIT_PER_MINUTE = 30;

    @Resource
    private FaqMapper faqMapper;

    @Resource
    private SupportContactMapper contactMapper;

    @Resource
    private SupportInquiryLogMapper inquiryLogMapper;

    @Resource
    private ConfigApi configApi;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public AppSupportBootstrapRespVO bootstrap(String locale, String market) {
        AppSupportBootstrapRespVO resp = new AppSupportBootstrapRespVO();
        resp.setGreeting(defaultGreeting(normalizeLocale(locale)));
        resp.setContacts(toContactRespList(listContacts(market)));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppSupportAskRespVO ask(AppSupportAskReqVO reqVO, Long userId, String ip, String userAgent) {
        enforceRateLimit(reqVO.getSessionId());

        String locale = normalizeLocale(reqVO.getLocale());
        String market = normalizeMarket(reqVO.getMarket());
        MatchResult match = matchFaq(reqVO.getQuestion(), locale);
        boolean accepted = match.getFaq() != null && match.getScore().compareTo(getThreshold()) >= 0;

        SupportInquiryLogDO logDO = new SupportInquiryLogDO();
        logDO.setUserId(userId);
        logDO.setSessionId(reqVO.getSessionId());
        logDO.setLocale(locale);
        logDO.setMarket(market);
        logDO.setQuestionText(reqVO.getQuestion().trim());
        logDO.setMatchedFaqId(accepted ? match.getFaq().getId() : null);
        logDO.setScore(match.getScore());
        logDO.setClickedToHuman(false);
        logDO.setIp(ip);
        logDO.setUserAgent(truncate(userAgent, 512));
        // @PermitAll 公开接口无 SecurityContext,MetaObjectHandler 填不到 creator/updater,
        // 走 DEFAULT '' fallback 又被 MyBatis-Plus 主动写成 null 触发 NOT NULL 报错。显式置 ''。
        applyAnonymousAudit(logDO);
        inquiryLogMapper.insert(logDO);

        List<AppSupportContactRespVO> contacts = toContactRespList(listContacts(market));
        AppSupportAskRespVO resp = new AppSupportAskRespVO();
        resp.setLogId(logDO.getId());
        resp.setMatched(accepted);
        resp.setScore(match.getScore());
        resp.setContacts(contacts);
        if (accepted) {
            FaqDO faq = match.getFaq();
            resp.setFaqId(faq.getId());
            resp.setQuestion(faq.getQuestion());
            resp.setAnswer(faq.getAnswer());
            faqMapper.incrementMatchCount(faq.getId());
        } else {
            resp.setFallbackMessage(defaultFallback(locale));
        }
        return resp;
    }

    @Override
    public void markContactClicked(Long logId, Long contactId, String sessionId, String locale,
                                   String market, Long userId, String ip, String userAgent) {
        if (logId != null) {
            SupportInquiryLogDO update = new SupportInquiryLogDO();
            update.setId(logId);
            update.setClickedToHuman(true);
            update.setClickedContactId(contactId);
            inquiryLogMapper.updateById(update);
            return;
        }
        // Phase 1 直接渠道点击:新建一行纯点击日志,question_text 留空
        SupportInquiryLogDO logDO = new SupportInquiryLogDO();
        logDO.setUserId(userId);
        logDO.setSessionId(sessionId);
        logDO.setLocale(normalizeLocale(locale));
        logDO.setMarket(normalizeMarket(market));
        logDO.setQuestionText(null);
        logDO.setMatchedFaqId(null);
        logDO.setScore(null);
        logDO.setClickedToHuman(true);
        logDO.setClickedContactId(contactId);
        logDO.setIp(ip);
        logDO.setUserAgent(truncate(userAgent, 512));
        applyAnonymousAudit(logDO);
        inquiryLogMapper.insert(logDO);
    }

    @Override
    public PageResult<FaqDO> getFaqPage(SupportFaqPageReqVO reqVO) {
        return faqMapper.selectPage(reqVO);
    }

    @Override
    public FaqDO getFaq(Long id) {
        return faqMapper.selectById(id);
    }

    @Override
    public Long createFaq(SupportFaqSaveReqVO reqVO) {
        ensureUniqueFaqQuestion(reqVO, null);
        FaqDO entity = BeanUtils.toBean(reqVO, FaqDO.class);
        applyFaqDefaults(entity);
        faqMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateFaq(SupportFaqSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "更新 FAQ 时 id 必填");
        }
        if (faqMapper.selectById(reqVO.getId()) == null) {
            throw ServiceExceptionUtil.exception0(404, "FAQ 不存在");
        }
        ensureUniqueFaqQuestion(reqVO, reqVO.getId());
        FaqDO entity = BeanUtils.toBean(reqVO, FaqDO.class);
        applyFaqDefaults(entity);
        faqMapper.updateById(entity);
    }

    @Override
    public void deleteFaq(Long id) {
        if (faqMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception0(404, "FAQ 不存在");
        }
        faqMapper.deleteById(id);
    }

    @Override
    public PageResult<SupportContactDO> getContactPage(SupportContactPageReqVO reqVO) {
        return contactMapper.selectPage(reqVO);
    }

    @Override
    public SupportContactDO getContact(Long id) {
        return contactMapper.selectById(id);
    }

    @Override
    public Long createContact(SupportContactSaveReqVO reqVO) {
        SupportContactDO entity = BeanUtils.toBean(reqVO, SupportContactDO.class);
        applyContactDefaults(entity);
        contactMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateContact(SupportContactSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "更新联系方式时 id 必填");
        }
        if (contactMapper.selectById(reqVO.getId()) == null) {
            throw ServiceExceptionUtil.exception0(404, "联系方式不存在");
        }
        SupportContactDO entity = BeanUtils.toBean(reqVO, SupportContactDO.class);
        applyContactDefaults(entity);
        contactMapper.updateById(entity);
    }

    @Override
    public void deleteContact(Long id) {
        if (contactMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception0(404, "联系方式不存在");
        }
        contactMapper.deleteById(id);
    }

    @Override
    public PageResult<SupportInquiryLogDO> getInquiryPage(SupportInquiryPageReqVO reqVO) {
        return inquiryLogMapper.selectPage(reqVO);
    }

    @Override
    public SupportInquiryStatsRespVO getInquiryStats() {
        List<SupportInquiryLogDO> logs = inquiryLogMapper.selectList(Wrappers.lambdaQuery());
        long total = logs.size();
        long ask = logs.stream()
                .filter(log -> log.getQuestionText() != null && !log.getQuestionText().isBlank())
                .count();
        long matched = logs.stream().filter(log -> log.getMatchedFaqId() != null).count();
        long clicked = logs.stream().filter(log -> Boolean.TRUE.equals(log.getClickedToHuman())).count();
        long directClick = total - ask;
        SupportInquiryStatsRespVO resp = new SupportInquiryStatsRespVO();
        resp.setTotalCount(total);
        resp.setAskCount(ask);
        resp.setMatchedCount(matched);
        resp.setUnmatchedCount(Math.max(ask - matched, 0L));
        resp.setClickedToHumanCount(clicked);
        resp.setDirectClickCount(directClick);
        resp.setMatchRate(rate(matched, ask));
        resp.setClickRate(rate(clicked, total));
        return resp;
    }

    @Override
    public List<SupportTopUnmatchedRespVO> getTopUnmatched(Integer limit) {
        int cappedLimit = Math.min(Math.max(limit == null ? 10 : limit, 1), 50);
        List<SupportInquiryLogDO> logs = inquiryLogMapper.selectList(Wrappers.<SupportInquiryLogDO>lambdaQuery()
                .isNull(SupportInquiryLogDO::getMatchedFaqId)
                .isNotNull(SupportInquiryLogDO::getQuestionText));
        if (logs.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, List<SupportInquiryLogDO>> grouped = logs.stream()
                .filter(log -> log.getQuestionText() != null && !log.getQuestionText().isBlank())
                .collect(Collectors.groupingBy(
                        log -> log.getQuestionText().trim(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        return grouped.entrySet().stream()
                .map(entry -> {
                    SupportTopUnmatchedRespVO vo = new SupportTopUnmatchedRespVO();
                    vo.setQuestionText(entry.getKey());
                    vo.setCount((long) entry.getValue().size());
                    vo.setLastAskedAt(entry.getValue().stream()
                            .map(SupportInquiryLogDO::getCreateTime)
                            .filter(Objects::nonNull)
                            .max(Comparator.naturalOrder())
                            .orElse(null));
                    return vo;
                })
                .sorted(Comparator
                        .comparing(SupportTopUnmatchedRespVO::getCount, Comparator.reverseOrder())
                        .thenComparing(SupportTopUnmatchedRespVO::getLastAskedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(cappedLimit)
                .toList();
    }

    MatchResult matchFaq(String question, String locale) {
        List<String> fallbackLocales = localeFallbacks(locale);
        List<FaqDO> candidates = faqMapper.selectActiveByLocales(fallbackLocales);
        if (candidates.isEmpty()) {
            return MatchResult.empty();
        }

        String normalizedQuestion = normalizeText(question);
        MatchResult best = MatchResult.empty();
        for (FaqDO faq : candidates) {
            BigDecimal score = scoreFaq(normalizedQuestion, faq);
            int bestLocaleRank = fallbackLocales.indexOf(best.getFaq() == null ? "" : best.getFaq().getLocale());
            int currentLocaleRank = fallbackLocales.indexOf(faq.getLocale());
            if (score.compareTo(best.getScore()) > 0
                    || (score.compareTo(best.getScore()) == 0 && currentLocaleRank >= 0
                    && (bestLocaleRank < 0 || currentLocaleRank < bestLocaleRank))) {
                best = MatchResult.of(faq, score);
            }
        }
        return best;
    }

    BigDecimal scoreFaq(String normalizedQuestion, FaqDO faq) {
        BigDecimal score = BigDecimal.ZERO;
        List<String> keywords = faq.getKeywords();
        if (keywords != null && !keywords.isEmpty()) {
            boolean anyKeywordHit = keywords.stream()
                    .map(this::normalizeText)
                    .filter(s -> !s.isBlank())
                    .anyMatch(normalizedQuestion::contains);
            if (anyKeywordHit) {
                score = score.add(BigDecimal.ONE.divide(
                        BigDecimal.valueOf(keywords.size()), 3, RoundingMode.HALF_UP));
            }
        }

        String faqQuestion = normalizeText(faq.getQuestion());
        if (!faqQuestion.isBlank()
                && (normalizedQuestion.contains(faqQuestion) || faqQuestion.contains(normalizedQuestion))) {
            score = score.add(new BigDecimal("0.500"));
        }

        BigDecimal ngramScore = ngramOverlap(normalizedQuestion, faqQuestion)
                .multiply(new BigDecimal("0.300"));
        score = score.add(ngramScore);
        if (score.compareTo(BigDecimal.ONE) > 0) {
            score = BigDecimal.ONE;
        }
        return score.setScale(3, RoundingMode.HALF_UP);
    }

    List<SupportContactDO> listContacts(String market) {
        String normalized = normalizeMarket(market);
        List<SupportContactDO> contacts = contactMapper.selectActiveByMarket(normalized);
        if (!contacts.isEmpty() || DEFAULT_MARKET.equals(normalized)) {
            return contacts;
        }
        return contactMapper.selectActiveByMarket(DEFAULT_MARKET);
    }

    private BigDecimal getThreshold() {
        try {
            String raw = configApi.getConfigValueByKey(CONFIG_KEY_THRESHOLD);
            if (raw != null && !raw.isBlank()) {
                return new BigDecimal(raw).setScale(3, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            log.debug("[support] use default threshold, config read failed", e);
        }
        return DEFAULT_THRESHOLD;
    }

    private void ensureUniqueFaqQuestion(SupportFaqSaveReqVO reqVO, Long updatingId) {
        FaqDO existing = faqMapper.selectOne(Wrappers.<FaqDO>lambdaQuery()
                .eq(FaqDO::getCategory, reqVO.getCategory().trim())
                .eq(FaqDO::getLocale, normalizeLocale(reqVO.getLocale()))
                .eq(FaqDO::getQuestion, reqVO.getQuestion().trim())
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), updatingId)) {
            throw ServiceExceptionUtil.exception0(409, "同分类/语言下已存在相同 FAQ 问题");
        }
    }

    private void applyFaqDefaults(FaqDO entity) {
        entity.setCategory(entity.getCategory().trim());
        entity.setLocale(normalizeLocale(entity.getLocale()));
        entity.setQuestion(entity.getQuestion().trim());
        entity.setAnswer(entity.getAnswer().trim());
        entity.setStatus(normalizeFaqStatus(entity.getStatus()));
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getKeywords() != null) {
            entity.setKeywords(entity.getKeywords().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(keyword -> !keyword.isBlank())
                    .distinct()
                    .toList());
        }
    }

    private String normalizeFaqStatus(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        if (!"active".equals(normalized) && !"disabled".equals(normalized)) {
            throw ServiceExceptionUtil.exception0(400, "FAQ 状态只支持 active / disabled");
        }
        return normalized;
    }

    private void applyContactDefaults(SupportContactDO entity) {
        entity.setMarket(normalizeMarket(entity.getMarket()));
        entity.setChannelType(entity.getChannelType().trim().toLowerCase(Locale.ROOT));
        entity.setDisplayText(entity.getDisplayText().trim());
        if (entity.getLinkUrl() != null) {
            entity.setLinkUrl(entity.getLinkUrl().trim());
        }
        if (entity.getImageUrl() != null) {
            entity.setImageUrl(entity.getImageUrl().trim());
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        if ((entity.getLinkUrl() == null || entity.getLinkUrl().isBlank())
                && (entity.getImageUrl() == null || entity.getImageUrl().isBlank())) {
            throw ServiceExceptionUtil.exception0(400, "外链和二维码图片至少填写一个");
        }
    }

    private BigDecimal rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    private void enforceRateLimit(String sessionId) {
        String key = "support:ask:rate:" + sessionId;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofMinutes(1));
        }
        if (count != null && count > RATE_LIMIT_PER_MINUTE) {
            throw exception(SUPPORT_RATE_LIMIT);
        }
    }

    private BigDecimal ngramOverlap(String source, String target) {
        Set<String> sourceGrams = grams(source);
        Set<String> targetGrams = grams(target);
        if (sourceGrams.isEmpty() || targetGrams.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Set<String> intersection = new HashSet<>(sourceGrams);
        intersection.retainAll(targetGrams);
        return BigDecimal.valueOf(intersection.size())
                .divide(BigDecimal.valueOf(sourceGrams.size()), 3, RoundingMode.HALF_UP);
    }

    private Set<String> grams(String value) {
        Set<String> grams = new LinkedHashSet<>();
        String cleaned = value.replace(" ", "");
        for (int n = 2; n <= 3; n++) {
            if (cleaned.length() < n) {
                continue;
            }
            for (int i = 0; i <= cleaned.length() - n; i++) {
                grams.add(cleaned.substring(i, i + n));
            }
        }
        return grams;
    }

    private List<String> localeFallbacks(String locale) {
        List<String> locales = new ArrayList<>();
        String normalized = normalizeLocale(locale);
        locales.add(normalized);
        if (!DEFAULT_LOCALE.equals(normalized)) {
            locales.add(DEFAULT_LOCALE);
        }
        if (!"zh-CN".equals(normalized)) {
            locales.add("zh-CN");
        }
        return locales;
    }

    private String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return DEFAULT_LOCALE;
        }
        String normalized = locale.trim().replace('_', '-').toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "zh-cn" -> "zh-CN";
            case "zh-tw" -> "zh-TW";
            case "ar" -> "ar";
            default -> DEFAULT_LOCALE;
        };
    }

    private String normalizeMarket(String market) {
        if (market == null || market.isBlank()) {
            return DEFAULT_MARKET;
        }
        return market.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String input) {
        if (input == null) {
            return "";
        }
        return Normalizer.normalize(input, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\p{Punct}+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private List<AppSupportContactRespVO> toContactRespList(List<SupportContactDO> contacts) {
        return BeanUtils.toBean(contacts, AppSupportContactRespVO.class);
    }

    private String defaultGreeting(String locale) {
        return switch (locale) {
            case "zh-CN" -> "Hi! 输入您的问题,我帮您查找答案。或直接联系真人客服。";
            case "zh-TW" -> "Hi! 輸入您的問題,我幫您查找答案。或直接聯絡真人客服。";
            case "ar" -> "مرحبًا! اكتب سؤالك وسنبحث لك عن الإجابة، أو تواصل مع الدعم مباشرة.";
            default -> "Hi! Ask a question and we will look for an answer, or contact support directly.";
        };
    }

    private String defaultFallback(String locale) {
        return switch (locale) {
            case "zh-CN" -> "暂时没有找到合适答案,您可以联系真人客服。";
            case "zh-TW" -> "暫時沒有找到合適答案,您可以聯絡真人客服。";
            case "ar" -> "لم نجد إجابة مناسبة الآن. يمكنك التواصل مع الدعم مباشرة.";
            default -> "We could not find a matching answer yet. You can contact support directly.";
        };
    }

    /**
     * 公开 @PermitAll 接口写入审计字段兜底:无 SecurityContext 时 MetaObjectHandler 填 null
     * 会触发 creator NOT NULL 报错(实证 D24 contact-click insert 路径)。显式置 ''。
     */
    private void applyAnonymousAudit(SupportInquiryLogDO logDO) {
        if (logDO.getCreator() == null) {
            logDO.setCreator("");
        }
        if (logDO.getUpdater() == null) {
            logDO.setUpdater("");
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    @Data
    static class MatchResult {
        private final FaqDO faq;
        private final BigDecimal score;

        static MatchResult empty() {
            return new MatchResult(null, BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
        }

        static MatchResult of(FaqDO faq, BigDecimal score) {
            return new MatchResult(faq, score);
        }
    }
}
