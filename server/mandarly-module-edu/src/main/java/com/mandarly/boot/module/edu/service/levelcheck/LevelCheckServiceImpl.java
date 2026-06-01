package com.mandarly.boot.module.edu.service.levelcheck;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckLevelStatsRespVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckOptionRespVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckOptionSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionRespVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckQuestionSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckSubmissionPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.levelcheck.vo.LevelCheckSubmissionStatsRespVO;
import com.mandarly.boot.module.edu.controller.app.levelcheck.vo.AppLevelCheckResultRespVO;
import com.mandarly.boot.module.edu.controller.app.levelcheck.vo.AppLevelCheckSubmitReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckOptionDO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckQuestionDO;
import com.mandarly.boot.module.edu.dal.dataobject.levelcheck.LevelCheckSubmissionDO;
import com.mandarly.boot.module.edu.dal.dataobject.pkg.PackageDO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.levelcheck.LevelCheckOptionMapper;
import com.mandarly.boot.module.edu.dal.mysql.levelcheck.LevelCheckQuestionMapper;
import com.mandarly.boot.module.edu.dal.mysql.levelcheck.LevelCheckSubmissionMapper;
import com.mandarly.boot.module.edu.dal.mysql.pkg.PackageMapper;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import com.mandarly.boot.module.edu.enums.user.UserRoleEnum;
import com.mandarly.boot.module.edu.enums.user.UserStatusEnum;
import com.mandarly.boot.module.edu.service.i18n.I18nMessageService;
import com.mandarly.boot.module.edu.service.teacher.TeacherProfileService;
import com.mandarly.boot.module.system.service.user.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LevelCheckServiceImpl implements LevelCheckService {

    private static final int TOP_N = 3;

    @Resource
    private LevelCheckQuestionMapper questionMapper;

    @Resource
    private LevelCheckOptionMapper optionMapper;

    @Resource
    private LevelCheckSubmissionMapper submissionMapper;

    @Resource
    private TeacherProfileMapper teacherProfileMapper;

    @Resource
    private TeacherProfileService teacherProfileService;

    @Resource
    private EduUserMapper eduUserMapper;

    @Resource
    private PackageMapper packageMapper;

    @Resource
    private I18nMessageService i18nMessageService;

    @Resource
    private UserService userService;

    // ========== 题库 ==========

    @Override
    public List<LevelCheckQuestionRespVO> listActiveQuestionsWithOptions() {
        List<LevelCheckQuestionDO> questions = questionMapper.selectActiveOrderBySort();
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }
        return assembleQuestionsWithOptions(questions);
    }

    @Override
    public PageResult<LevelCheckQuestionDO> getQuestionPage(LevelCheckQuestionPageReqVO reqVO) {
        return questionMapper.selectPage(reqVO);
    }

    @Override
    public LevelCheckQuestionRespVO getQuestionWithOptions(Long id) {
        LevelCheckQuestionDO q = questionMapper.selectById(id);
        if (q == null) {
            return null;
        }
        return assembleQuestionsWithOptions(List.of(q)).get(0);
    }

    @Override
    public Long createQuestion(LevelCheckQuestionSaveReqVO reqVO) {
        LevelCheckQuestionDO existing = questionMapper.selectOne(Wrappers.<LevelCheckQuestionDO>lambdaQuery()
                .eq(LevelCheckQuestionDO::getQuestionCode, reqVO.getQuestionCode()));
        if (existing != null) {
            throw ServiceExceptionUtil.exception0(409, "questionCode 已存在");
        }
        LevelCheckQuestionDO entity = BeanUtils.toBean(reqVO, LevelCheckQuestionDO.class);
        if (entity.getQuestionType() == null) {
            entity.setQuestionType("single_choice");
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        questionMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateQuestion(LevelCheckQuestionSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "更新时 id 必填");
        }
        if (questionMapper.selectById(reqVO.getId()) == null) {
            throw ServiceExceptionUtil.exception0(404, "题目不存在");
        }
        questionMapper.updateById(BeanUtils.toBean(reqVO, LevelCheckQuestionDO.class));
    }

    @Override
    public void deleteQuestion(Long id) {
        if (questionMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception0(404, "题目不存在");
        }
        questionMapper.deleteById(id);
    }

    // ========== 选项 ==========

    @Override
    public List<LevelCheckOptionDO> listOptionsByQuestion(Long questionId) {
        return optionMapper.selectListByQuestionId(questionId);
    }

    @Override
    public Long createOption(LevelCheckOptionSaveReqVO reqVO) {
        if (questionMapper.selectById(reqVO.getQuestionId()) == null) {
            throw ServiceExceptionUtil.exception0(404, "题目不存在");
        }
        LevelCheckOptionDO entity = BeanUtils.toBean(reqVO, LevelCheckOptionDO.class);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        optionMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateOption(LevelCheckOptionSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "更新时 id 必填");
        }
        if (optionMapper.selectById(reqVO.getId()) == null) {
            throw ServiceExceptionUtil.exception0(404, "选项不存在");
        }
        optionMapper.updateById(BeanUtils.toBean(reqVO, LevelCheckOptionDO.class));
    }

    @Override
    public void deleteOption(Long id) {
        if (optionMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception0(404, "选项不存在");
        }
        optionMapper.deleteById(id);
    }

    // ========== 答卷与推荐 ==========

    @Override
    public AppLevelCheckResultRespVO submitAnswers(AppLevelCheckSubmitReqVO reqVO, Long userIdIfLogin) {
        // 1. 解析答案,定位每个 option 的规则
        Map<String, LevelCheckOptionDO> chosenOptions = resolveChosenOptions(reqVO.getAnswers());

        // 2. 推断等级(取 Q1 命中选项的 inferredLevel,缺省 beginner)
        String inferredLevel = chosenOptions.values().stream()
                .map(LevelCheckOptionDO::getInferredLevel)
                .filter(s -> s != null && !s.isBlank())
                .findFirst()
                .orElse("beginner");

        // 3. 教师候选池:approved + active
        List<TeacherProfileDO> approvedProfiles = teacherProfileMapper.selectList(
                Wrappers.<TeacherProfileDO>lambdaQuery()
                        .eq(TeacherProfileDO::getAuditStatus, TeacherAuditStatusEnum.APPROVED.getCode()));
        if (approvedProfiles.isEmpty()) {
            return persistAndBuildResult(reqVO, userIdIfLogin, inferredLevel,
                    Collections.emptyList(), null, chosenOptions.values());
        }
        Set<Long> activeTeacherIds = eduUserMapper.selectList(Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getRole, UserRoleEnum.TEACHER.getCode())
                        .eq(UserDO::getStatus, UserStatusEnum.ACTIVE.getCode()))
                .stream().map(UserDO::getId).collect(Collectors.toSet());
        List<TeacherProfileDO> candidates = approvedProfiles.stream()
                .filter(p -> activeTeacherIds.contains(p.getUserId()))
                .toList();
        if (candidates.isEmpty()) {
            return persistAndBuildResult(reqVO, userIdIfLogin, inferredLevel,
                    Collections.emptyList(), null, chosenOptions.values());
        }

        // 4. 强约束:并集所有选项的 matchExpertise(非空时,教师 expertise 必须含全部硬约束 tag)
        Set<String> hardConstraints = chosenOptions.values().stream()
                .map(LevelCheckOptionDO::getMatchExpertise)
                .filter(list -> list != null && !list.isEmpty())
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        List<TeacherProfileDO> filtered = candidates;
        if (!hardConstraints.isEmpty()) {
            filtered = candidates.stream()
                    .filter(p -> p.getExpertise() != null
                            && new HashSet<>(p.getExpertise()).containsAll(hardConstraints))
                    .toList();
        }
        // 强约束过滤后空了 → 退回到无强约束候选(避免空推荐)
        boolean fellBackFromHard = filtered.isEmpty() && !hardConstraints.isEmpty();
        if (fellBackFromHard) {
            log.warn("[submitAnswers] 强约束 {} 后无候选,降级到全候选池", hardConstraints);
            filtered = candidates;
        }

        // 5. 软约束打分
        Map<Long, Integer> scoreMap = new HashMap<>();
        for (TeacherProfileDO p : filtered) {
            scoreMap.put(p.getUserId(), computeSoftScore(p, chosenOptions.values()));
        }

        // 6. 取 Top N(score 降 / yearsExperience 降 / id 升)
        Map<Long, TeacherProfileDO> profileById = filtered.stream()
                .collect(Collectors.toMap(TeacherProfileDO::getUserId, p -> p, (a, b) -> a));
        List<TeacherProfileDO> topN = filtered.stream()
                .sorted(Comparator
                        .comparing((TeacherProfileDO p) -> scoreMap.getOrDefault(p.getUserId(), 0))
                        .reversed()
                        .thenComparing(Comparator.comparing(
                                (TeacherProfileDO p) -> nullSafeInt(p.getYearsExperience())).reversed())
                        .thenComparing(TeacherProfileDO::getUserId))
                .limit(TOP_N)
                .toList();
        // 不足 TOP_N 时从未被选中的候选随机补
        if (topN.size() < TOP_N) {
            List<TeacherProfileDO> remaining = new ArrayList<>(filtered);
            remaining.removeAll(topN);
            Collections.shuffle(remaining, new Random());
            int need = TOP_N - topN.size();
            List<TeacherProfileDO> filled = new ArrayList<>(topN);
            filled.addAll(remaining.subList(0, Math.min(need, remaining.size())));
            topN = filled;
        }

        // 7. 推荐套餐(按 Q3 weeklyCount → 找 active + weekly_count 匹配 + 有效期最短)
        Integer recommendedWeeklyCount = chosenOptions.values().stream()
                .map(LevelCheckOptionDO::getRecommendedWeeklyCount)
                .filter(n -> n != null)
                .findFirst()
                .orElse(null);
        PackageDO recommendedPkg = recommendedWeeklyCount != null
                ? packageMapper.selectOne(Wrappers.<PackageDO>lambdaQuery()
                        .eq(PackageDO::getIsActive, true)
                        .eq(PackageDO::getIsFreeTrial, false)
                        .eq(PackageDO::getWeeklyCount, recommendedWeeklyCount)
                        .orderByAsc(PackageDO::getValidityDays)
                        .last("LIMIT 1"))
                : null;

        return persistAndBuildResult(reqVO, userIdIfLogin, inferredLevel, topN, recommendedPkg, chosenOptions.values());
    }

    @Override
    public AppLevelCheckResultRespVO getResult(Long submissionId) {
        LevelCheckSubmissionDO s = submissionMapper.selectById(submissionId);
        if (s == null) {
            throw ServiceExceptionUtil.exception0(404, "答卷不存在");
        }
        // 重新组装 RespVO(教师/套餐字段从 id 回查最新状态)
        AppLevelCheckResultRespVO resp = new AppLevelCheckResultRespVO();
        resp.setSubmissionId(s.getId());
        resp.setInferredLevel(s.getInferredLevel());
        resp.setRecommendedTeachers(loadTeachersByIds(s.getRecommendedTeacherIds()));
        if (s.getRecommendedPackageId() != null) {
            resp.setRecommendedPackage(buildPackageResp(packageMapper.selectById(s.getRecommendedPackageId()), s.getLocale()));
        }
        return resp;
    }

    @Override
    public PageResult<LevelCheckSubmissionDO> getSubmissionPage(LevelCheckSubmissionPageReqVO reqVO) {
        return submissionMapper.selectPage(reqVO);
    }

    @Override
    public LevelCheckSubmissionStatsRespVO getSubmissionStats() {
        List<LevelCheckSubmissionDO> submissions = submissionMapper.selectList(Wrappers.lambdaQuery());
        long total = submissions.size();
        long converted = submissions.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsConverted()))
                .count();
        Map<String, Long> levelCounts = submissions.stream()
                .collect(Collectors.groupingBy(LevelCheckSubmissionDO::getInferredLevel,
                        LinkedHashMap::new, Collectors.counting()));

        LevelCheckSubmissionStatsRespVO resp = new LevelCheckSubmissionStatsRespVO();
        resp.setTotalCount(total);
        resp.setConvertedCount(converted);
        resp.setUnconvertedCount(total - converted);
        resp.setConversionRate(rate(converted, total));
        resp.setLevelStats(levelCounts.entrySet().stream()
                .map(entry -> {
                    LevelCheckLevelStatsRespVO item = new LevelCheckLevelStatsRespVO();
                    item.setInferredLevel(entry.getKey());
                    item.setCount(entry.getValue());
                    item.setRate(rate(entry.getValue(), total));
                    return item;
                })
                .toList());
        return resp;
    }

    // ========== 私有方法 ==========

    private List<LevelCheckQuestionRespVO> assembleQuestionsWithOptions(List<LevelCheckQuestionDO> questions) {
        List<Long> questionIds = questions.stream().map(LevelCheckQuestionDO::getId).toList();
        Map<Long, List<LevelCheckOptionRespVO>> optionsByQ = optionMapper.selectListByQuestionIds(questionIds)
                .stream()
                .map(o -> BeanUtils.toBean(o, LevelCheckOptionRespVO.class))
                .collect(Collectors.groupingBy(LevelCheckOptionRespVO::getQuestionId,
                        LinkedHashMap::new, Collectors.toList()));
        return questions.stream().map(q -> {
            LevelCheckQuestionRespVO vo = BeanUtils.toBean(q, LevelCheckQuestionRespVO.class);
            vo.setOptions(optionsByQ.getOrDefault(q.getId(), Collections.emptyList()));
            return vo;
        }).toList();
    }

    private Map<String, LevelCheckOptionDO> resolveChosenOptions(List<AppLevelCheckSubmitReqVO.Answer> answers) {
        Set<String> questionCodes = answers.stream()
                .map(AppLevelCheckSubmitReqVO.Answer::getQuestionCode)
                .collect(Collectors.toSet());
        if (questionCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        // 按 questionCode 取题目
        List<LevelCheckQuestionDO> qs = questionMapper.selectList(Wrappers.<LevelCheckQuestionDO>lambdaQuery()
                .in(LevelCheckQuestionDO::getQuestionCode, questionCodes));
        Map<Long, String> qIdToCode = qs.stream()
                .collect(Collectors.toMap(LevelCheckQuestionDO::getId, LevelCheckQuestionDO::getQuestionCode));
        // 按 questionId 取选项
        List<LevelCheckOptionDO> options = optionMapper.selectListByQuestionIds(qIdToCode.keySet());
        // index by (questionCode, optionCode) → option
        Map<String, LevelCheckOptionDO> indexed = new HashMap<>();
        for (LevelCheckOptionDO opt : options) {
            String qCode = qIdToCode.get(opt.getQuestionId());
            indexed.put(qCode + "::" + opt.getOptionCode(), opt);
        }
        Map<String, LevelCheckOptionDO> chosen = new LinkedHashMap<>();
        for (AppLevelCheckSubmitReqVO.Answer ans : answers) {
            LevelCheckOptionDO opt = indexed.get(ans.getQuestionCode() + "::" + ans.getOptionCode());
            if (opt == null) {
                throw ServiceExceptionUtil.exception0(400,
                        "答案非法:questionCode=" + ans.getQuestionCode() + " optionCode=" + ans.getOptionCode());
            }
            chosen.put(ans.getQuestionCode(), opt);
        }
        return chosen;
    }

    private int computeSoftScore(TeacherProfileDO profile, java.util.Collection<LevelCheckOptionDO> chosenOptions) {
        int score = 0;
        Set<String> profileExpertise = profile.getExpertise() == null
                ? Collections.emptySet() : new HashSet<>(profile.getExpertise());

        // 用户选项的 score_rules
        for (LevelCheckOptionDO opt : chosenOptions) {
            if (opt.getScoreRules() == null) continue;
            for (Map<String, Object> rule : opt.getScoreRules()) {
                Object expertise = rule.get("expertise");
                Object scoreObj = rule.get("score");
                if (expertise instanceof String exp && profileExpertise.contains(exp)
                        && scoreObj instanceof Number sn) {
                    score += sn.intValue();
                }
            }
        }
        // 教学年限 +5 / +10
        int years = nullSafeInt(profile.getYearsExperience());
        if (years >= 3) score += 5;
        if (years >= 5) score += 5;
        // 自我介绍视频 +2
        if (profile.getIntroVideoUrl() != null && !profile.getIntroVideoUrl().isBlank()) {
            score += 2;
        }
        return score;
    }

    private AppLevelCheckResultRespVO persistAndBuildResult(
            AppLevelCheckSubmitReqVO reqVO, Long userIdIfLogin, String inferredLevel,
            List<TeacherProfileDO> recommendedTeachers, PackageDO recommendedPkg,
            java.util.Collection<LevelCheckOptionDO> chosenOptions) {

        List<Long> teacherIds = recommendedTeachers.stream()
                .map(TeacherProfileDO::getUserId).toList();

        LevelCheckSubmissionDO submission = new LevelCheckSubmissionDO();
        // 未登录路径(@PermitAll)需手动兜底 creator/updater(若依 MetaObjectHandler 在无登录上下文时填 null,触发 NOT NULL)
        submission.setCreator("anonymous");
        submission.setUpdater("anonymous");
        submission.setUserId(userIdIfLogin);
        submission.setSessionId(reqVO.getSessionId());
        submission.setEmail(reqVO.getEmail());
        submission.setLocale(reqVO.getLocale());
        submission.setAnswers(reqVO.getAnswers().stream()
                .map(a -> Map.of("questionCode", a.getQuestionCode(), "optionCode", a.getOptionCode()))
                .toList());
        submission.setInferredLevel(inferredLevel);
        submission.setRecommendedTeacherIds(teacherIds);
        submission.setRecommendedPackageId(recommendedPkg == null ? null : recommendedPkg.getId());
        submission.setIsConverted(false);
        submissionMapper.insert(submission);

        Map<Long, UserDO> usersById = loadUsers(teacherIds);

        AppLevelCheckResultRespVO resp = new AppLevelCheckResultRespVO();
        resp.setSubmissionId(submission.getId());
        resp.setInferredLevel(inferredLevel);
        resp.setRecommendedTeachers(recommendedTeachers.stream()
                .map(p -> buildTeacherResp(p, usersById.get(p.getUserId()))).toList());
        if (recommendedPkg != null) {
            resp.setRecommendedPackage(buildPackageResp(recommendedPkg, reqVO.getLocale()));
        }

        log.info("[submitAnswers] submissionId={} level={} teachers={} pkg={}",
                submission.getId(), inferredLevel, teacherIds, submission.getRecommendedPackageId());
        return resp;
    }

    private AppLevelCheckResultRespVO.RecommendedTeacher buildTeacherResp(TeacherProfileDO p, UserDO user) {
        AppLevelCheckResultRespVO.RecommendedTeacher t = new AppLevelCheckResultRespVO.RecommendedTeacher();
        t.setUserId(p.getUserId());
        if (user != null) {
            t.setNickname(user.getNickname() == null ? "" : user.getNickname());
            t.setAvatar(userService.presignAvatarUrl(user.getAvatarUrl()));
        }
        t.setIntro(p.getIntro());
        t.setExpertise(p.getExpertise());
        t.setAccent(p.getAccent());
        t.setLanguages(p.getLanguages());
        t.setYearsExperience(p.getYearsExperience());
        // D19 续: DB 存 object key, 出参签成 COS 15min URL 供等级测试结果页预览试讲视频
        t.setIntroVideoUrl(teacherProfileService.presignIntroVideoUrl(p.getIntroVideoUrl()));
        return t;
    }

    private Map<Long, UserDO> loadUsers(java.util.Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();
        return eduUserMapper.selectBatchIds(userIds)
                .stream().collect(Collectors.toMap(UserDO::getId, u -> u, (a, b) -> a));
    }

    private AppLevelCheckResultRespVO.RecommendedPackage buildPackageResp(PackageDO pkg, String locale) {
        if (pkg == null) return null;
        AppLevelCheckResultRespVO.RecommendedPackage rp = new AppLevelCheckResultRespVO.RecommendedPackage();
        rp.setId(pkg.getId());
        rp.setNameI18nCode(pkg.getNameI18nCode());
        rp.setName(i18nMessageService.translate(pkg.getNameI18nCode(), locale));
        rp.setWeeklyCount(pkg.getWeeklyCount());
        rp.setTotalCount(pkg.getTotalCount());
        rp.setValidityDays(pkg.getValidityDays());
        rp.setPriceVisible(false);
        rp.setIsFreeTrial(pkg.getIsFreeTrial());
        return rp;
    }

    private List<AppLevelCheckResultRespVO.RecommendedTeacher> loadTeachersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<TeacherProfileDO> list = teacherProfileMapper.selectList(
                Wrappers.<TeacherProfileDO>lambdaQuery().in(TeacherProfileDO::getUserId, ids));
        Map<Long, TeacherProfileDO> byId = list.stream()
                .collect(Collectors.toMap(TeacherProfileDO::getUserId, p -> p));
        Map<Long, UserDO> usersById = loadUsers(ids);
        return ids.stream().map(byId::get).filter(p -> p != null)
                .map(p -> buildTeacherResp(p, usersById.get(p.getUserId()))).toList();
    }

    private static int nullSafeInt(Integer v) {
        return v == null ? 0 : v;
    }

    private BigDecimal rate(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(part)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }

}
