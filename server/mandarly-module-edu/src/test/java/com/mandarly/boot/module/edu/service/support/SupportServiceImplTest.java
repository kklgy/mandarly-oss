package com.mandarly.boot.module.edu.service.support;

import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportFaqSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.support.vo.SupportInquiryStatsRespVO;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportAskReqVO;
import com.mandarly.boot.module.edu.controller.app.support.vo.AppSupportAskRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.support.FaqDO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportContactDO;
import com.mandarly.boot.module.edu.dal.dataobject.support.SupportInquiryLogDO;
import com.mandarly.boot.module.edu.dal.mysql.support.FaqMapper;
import com.mandarly.boot.module.edu.dal.mysql.support.SupportContactMapper;
import com.mandarly.boot.module.edu.dal.mysql.support.SupportInquiryLogMapper;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupportServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SupportServiceImpl supportService;

    @Mock
    private FaqMapper faqMapper;
    @Mock
    private SupportContactMapper contactMapper;
    @Mock
    private SupportInquiryLogMapper inquiryLogMapper;
    @Mock
    private ConfigApi configApi;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void ask_keywordHit_returnsFaqAndWritesMatchedLog() {
        FaqDO faq = faq(1L, "zh-CN", "如何预约课程?", "打开老师页选择时间。", List.of("预约", "上课"));
        when(faqMapper.selectActiveByLocales(List.of("zh-CN", "en"))).thenReturn(List.of(faq));
        when(contactMapper.selectActiveByMarket("DEFAULT")).thenReturn(List.of(contact()));
        when(configApi.getConfigValueByKey("support.faq_match_threshold")).thenReturn("0.300");
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("support:ask:rate:s1")).thenReturn(1L);

        AppSupportAskRespVO resp = supportService.ask(req("s1", "zh-CN", "我想预约上课"), 10L,
                "127.0.0.1", "ua");

        assertThat(resp.getMatched()).isTrue();
        assertThat(resp.getFaqId()).isEqualTo(1L);
        assertThat(resp.getAnswer()).isEqualTo("打开老师页选择时间。");
        assertThat(resp.getScore()).isGreaterThanOrEqualTo(new BigDecimal("0.300"));
        verify(inquiryLogMapper).insert(any(SupportInquiryLogDO.class));
        verify(faqMapper).incrementMatchCount(1L);
    }

    @Test
    void ask_noMatch_returnsFallbackAndContacts() {
        FaqDO faq = faq(2L, "zh-CN", "套餐怎么收费?", "看套餐页。", List.of("套餐"));
        when(faqMapper.selectActiveByLocales(List.of("zh-CN", "en"))).thenReturn(List.of(faq));
        when(contactMapper.selectActiveByMarket("DEFAULT")).thenReturn(List.of(contact()));
        when(configApi.getConfigValueByKey("support.faq_match_threshold")).thenReturn("0.900");
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("support:ask:rate:s2")).thenReturn(1L);

        AppSupportAskRespVO resp = supportService.ask(req("s2", "zh-CN", "我要改发票"), null,
                "127.0.0.1", "ua");

        assertThat(resp.getMatched()).isFalse();
        assertThat(resp.getFaqId()).isNull();
        assertThat(resp.getFallbackMessage()).contains("真人客服");
        assertThat(resp.getContacts()).hasSize(1);
        verify(inquiryLogMapper).insert(any(SupportInquiryLogDO.class));
    }

    @Test
    void bootstrap_marketFallsBackToDefaultContacts() {
        when(contactMapper.selectActiveByMarket("GH")).thenReturn(List.of());
        when(contactMapper.selectActiveByMarket("DEFAULT")).thenReturn(List.of(contact()));

        assertThat(supportService.bootstrap("en", "GH").getContacts()).hasSize(1);
    }

    @Test
    void createFaq_normalizesLocaleStatusAndKeywords() {
        when(faqMapper.selectOne(any())).thenReturn(null);

        SupportFaqSaveReqVO reqVO = new SupportFaqSaveReqVO();
        reqVO.setCategory(" other ");
        reqVO.setLocale("zh_cn");
        reqVO.setQuestion(" 怎么预约课程? ");
        reqVO.setAnswer(" 打开老师页选择时间。 ");
        reqVO.setKeywords(List.of("预约", " 预约 ", "", "上课"));
        reqVO.setStatus("ACTIVE");

        supportService.createFaq(reqVO);

        ArgumentCaptor<FaqDO> captor = ArgumentCaptor.forClass(FaqDO.class);
        verify(faqMapper).insert(captor.capture());
        assertThat(captor.getValue().getCategory()).isEqualTo("other");
        assertThat(captor.getValue().getLocale()).isEqualTo("zh-CN");
        assertThat(captor.getValue().getStatus()).isEqualTo("active");
        assertThat(captor.getValue().getKeywords()).containsExactly("预约", "上课");
        assertThat(captor.getValue().getSort()).isZero();
    }

    @Test
    void getInquiryStats_countsMatchAndContactClickRates() {
        SupportInquiryLogDO matched = new SupportInquiryLogDO();
        matched.setQuestionText("如何预约课程?");
        matched.setMatchedFaqId(1L);
        matched.setClickedToHuman(false);
        SupportInquiryLogDO unmatchedClicked = new SupportInquiryLogDO();
        unmatchedClicked.setQuestionText("我要改发票");
        unmatchedClicked.setClickedToHuman(true);
        SupportInquiryLogDO directClick = new SupportInquiryLogDO();
        directClick.setQuestionText(null);
        directClick.setClickedToHuman(true);
        when(inquiryLogMapper.selectList(any()))
                .thenReturn(List.of(matched, unmatchedClicked, directClick));

        SupportInquiryStatsRespVO stats = supportService.getInquiryStats();

        assertThat(stats.getTotalCount()).isEqualTo(3);
        assertThat(stats.getAskCount()).isEqualTo(2);
        assertThat(stats.getDirectClickCount()).isEqualTo(1);
        assertThat(stats.getMatchedCount()).isEqualTo(1);
        assertThat(stats.getUnmatchedCount()).isEqualTo(1);
        assertThat(stats.getClickedToHumanCount()).isEqualTo(2);
        // matchRate 分母 = askCount(2) → 1/2 = 0.5
        assertThat(stats.getMatchRate()).isEqualByComparingTo("0.5000");
        // clickRate 分母 = totalCount(3) → 2/3 ≈ 0.6667
        assertThat(stats.getClickRate()).isEqualByComparingTo("0.6667");
    }

    @Test
    void markContactClicked_withLogId_updatesExistingLog() {
        supportService.markContactClicked(99L, 7L, "s9", "zh-CN", "HK",
                10L, "127.0.0.1", "ua");

        ArgumentCaptor<SupportInquiryLogDO> captor = ArgumentCaptor.forClass(SupportInquiryLogDO.class);
        verify(inquiryLogMapper).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(99L);
        assertThat(captor.getValue().getClickedToHuman()).isTrue();
        assertThat(captor.getValue().getClickedContactId()).isEqualTo(7L);
    }

    @Test
    void markContactClicked_withoutLogId_insertsDirectClickLog() {
        supportService.markContactClicked(null, 7L, "s9", "zh-CN", "HK",
                10L, "127.0.0.1", "ua");

        ArgumentCaptor<SupportInquiryLogDO> captor = ArgumentCaptor.forClass(SupportInquiryLogDO.class);
        verify(inquiryLogMapper).insert(captor.capture());
        SupportInquiryLogDO inserted = captor.getValue();
        assertThat(inserted.getSessionId()).isEqualTo("s9");
        assertThat(inserted.getLocale()).isEqualTo("zh-CN");
        assertThat(inserted.getMarket()).isEqualTo("HK");
        assertThat(inserted.getQuestionText()).isNull();
        assertThat(inserted.getMatchedFaqId()).isNull();
        assertThat(inserted.getClickedToHuman()).isTrue();
        assertThat(inserted.getClickedContactId()).isEqualTo(7L);
        assertThat(inserted.getUserId()).isEqualTo(10L);
    }

    private AppSupportAskReqVO req(String sessionId, String locale, String question) {
        AppSupportAskReqVO reqVO = new AppSupportAskReqVO();
        reqVO.setSessionId(sessionId);
        reqVO.setLocale(locale);
        reqVO.setQuestion(question);
        return reqVO;
    }

    private FaqDO faq(Long id, String locale, String question, String answer, List<String> keywords) {
        FaqDO faq = new FaqDO();
        faq.setId(id);
        faq.setLocale(locale);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setKeywords(keywords);
        faq.setStatus("active");
        return faq;
    }

    private SupportContactDO contact() {
        SupportContactDO contact = new SupportContactDO();
        contact.setId(1L);
        contact.setMarket("DEFAULT");
        contact.setChannelType("other");
        contact.setDisplayText("Contact us");
        contact.setLinkUrl("mailto:hello.com");
        contact.setIsActive(true);
        return contact;
    }
}
