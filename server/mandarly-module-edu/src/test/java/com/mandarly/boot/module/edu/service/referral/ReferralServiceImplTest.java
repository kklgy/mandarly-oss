package com.mandarly.boot.module.edu.service.referral;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.payment.PaymentDO;
import com.mandarly.boot.module.edu.dal.dataobject.referral.ReferralRecordDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.payment.PaymentMapper;
import com.mandarly.boot.module.edu.dal.mysql.referral.ReferralRecordMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.service.booking.StudentPackageService;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import com.mandarly.boot.module.system.api.mail.MailSendApi;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ReferralServiceImpl TDD 单元测试(5 case)
 *
 * <p>覆盖:
 * <ol>
 *   <li>bindAndCalculateDiscount:首次 + 有效码 → 返回 config discount</li>
 *   <li>bindAndCalculateDiscount:自引用 → 抛 REFERRAL_CODE_SELF_USE</li>
 *   <li>bindAndCalculateDiscount:已 bound → 返回 0,不抛错</li>
 *   <li>attachPayment:支付单创建后回填 paymentId</li>
 *   <li>triggerReward:存在 bound 记录 → 发放 free_trial + 标记 rewarded</li>
 *   <li>triggerReward:已 rewarded → 幂等 skip</li>
 * </ol>
 *
 * <p>注:反射调用 UserService.getUserByReferralCode 在单元测试中通过 spy 覆盖 findUserByReferralCode,
 * 避免 Spring context 依赖。
 */
class ReferralServiceImplTest extends BaseMockitoUnitTest {

    @Spy
    @InjectMocks
    private ReferralServiceImpl referralService;

    @Mock
    private ReferralRecordMapper referralRecordMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private EduUserMapper eduUserMapper;

    @Mock
    private ConfigApi configApi;

    @Mock
    private StudentPackageService studentPackageService;

    @Mock
    private MailSendApi mailSendApi;  // Phase 9.2:邮件接入 mock

    // ======================== tests: bindAndCalculateDiscount ========================

    /** case 1: 首次 + 有效码 → 返回 config discount */
    @Test
    void bindAndCalculateDiscount_firstTime_validCode_returnsConfiguredDiscount() {
        Long refereeUserId = 1L;
        String code = "VALID123";
        Long referrerId = 999L;

        // stub 反射方法(通过 spy 覆盖 findUserByReferralCode)
        doReturn(Optional.of(referrerId)).when(referralService).findUserByReferralCode(code);

        // 非自引用
        // referee 无 paid payment
        when(paymentMapper.existsPaidByUser(refereeUserId)).thenReturn(false);
        // 未 bound
        when(referralRecordMapper.selectByReferee(refereeUserId)).thenReturn(Optional.empty());
        // config discount = 20
        when(configApi.getConfigValueByKey("mandarly.referral.referee_discount_usd")).thenReturn("20");
        when(referralRecordMapper.insert(any(ReferralRecordDO.class))).thenReturn(1);

        BigDecimal discount = referralService.bindAndCalculateDiscount(refereeUserId, code);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("20"));
        verify(referralRecordMapper).insert(any(ReferralRecordDO.class));
    }

    /** case 2: 自引用 → 抛 REFERRAL_CODE_SELF_USE(1_004_002) */
    @Test
    void bindAndCalculateDiscount_selfReferral_throws_REFERRAL_CODE_SELF_USE() {
        Long userId = 2L;
        String code = "SELF9999";

        // referrerId == refereeUserId → 自引用
        doReturn(Optional.of(userId)).when(referralService).findUserByReferralCode(code);

        assertThatThrownBy(() -> referralService.bindAndCalculateDiscount(userId, code))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getCode()).isEqualTo(1_004_002));
    }

    /** case 3: 已 bound → 返回 0,不抛错(降级语义) */
    @Test
    void bindAndCalculateDiscount_alreadyBound_returnsZero_noThrow() {
        Long refereeUserId = 3L;
        String code = "OLDCODE";
        Long referrerId = 888L;

        doReturn(Optional.of(referrerId)).when(referralService).findUserByReferralCode(code);
        when(paymentMapper.existsPaidByUser(refereeUserId)).thenReturn(false);

        // 已 bound
        ReferralRecordDO existingBound = new ReferralRecordDO();
        existingBound.setId(1L);
        existingBound.setStatus("bound");
        when(referralRecordMapper.selectByReferee(refereeUserId)).thenReturn(Optional.of(existingBound));

        BigDecimal discount = referralService.bindAndCalculateDiscount(refereeUserId, code);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
        // 不应 insert 新记录
        verify(referralRecordMapper, never()).insert(any(ReferralRecordDO.class));
    }

    /** 注册时带推荐码但 checkout 未显式传码 → 使用 user.referred_by 绑定推荐记录 */
    @Test
    void bindAndCalculateDiscount_blankCode_usesRegisteredReferredBy() {
        Long refereeUserId = 4L;
        Long referrerId = 888L;
        String referrerCode = "MAND8888";

        UserDO referee = new UserDO();
        referee.setId(refereeUserId);
        referee.setReferredBy(referrerId);
        UserDO referrer = new UserDO();
        referrer.setId(referrerId);
        referrer.setReferralCode(referrerCode);

        when(eduUserMapper.selectById(refereeUserId)).thenReturn(referee);
        when(eduUserMapper.selectById(referrerId)).thenReturn(referrer);
        when(paymentMapper.existsPaidByUser(refereeUserId)).thenReturn(false);
        when(referralRecordMapper.selectByReferee(refereeUserId)).thenReturn(Optional.empty());
        when(configApi.getConfigValueByKey("mandarly.referral.referee_discount_usd")).thenReturn("4");
        when(referralRecordMapper.insert(any(ReferralRecordDO.class))).thenReturn(1);

        BigDecimal discount = referralService.bindAndCalculateDiscount(refereeUserId, null);

        assertThat(discount).isEqualByComparingTo(new BigDecimal("4"));
        verify(referralRecordMapper).insert(argThat((ReferralRecordDO r) ->
                r.getReferrerUserId().equals(referrerId)
                        && r.getRefereeUserId().equals(refereeUserId)
                        && referrerCode.equals(r.getReferralCode())
        ));
    }

    // ======================== tests: attachPayment ========================

    /** case 4: 支付单创建后回填 referral_record.payment_id */
    @Test
    void attachPayment_boundRecordWithoutPayment_backfillsPaymentId() {
        Long refereeUserId = 6L;
        Long paymentId = 66L;

        ReferralRecordDO record = new ReferralRecordDO();
        record.setId(16L);
        record.setStatus("bound");
        record.setPaymentId(null);
        when(referralRecordMapper.selectByReferee(refereeUserId)).thenReturn(Optional.of(record));
        when(referralRecordMapper.updateById(any(ReferralRecordDO.class))).thenReturn(1);

        referralService.attachPayment(refereeUserId, paymentId);

        verify(referralRecordMapper).updateById(argThat((ReferralRecordDO r) ->
                r.getId().equals(16L) && r.getPaymentId().equals(paymentId)
        ));
    }

    // ======================== tests: triggerReward ========================

    /** case 5: 存在 bound 记录 → 发放 free_trial(反射调 StudentPackageService) + 标记 rewarded */
    @Test
    void triggerReward_existingBound_grantsFreeTrialPackage_marksRewarded() {
        Long paymentId = 100L;
        Long referrerUserId = 777L;
        Long packageId = 55L;

        ReferralRecordDO record = new ReferralRecordDO();
        record.setId(10L);
        record.setReferrerUserId(referrerUserId);
        record.setPaymentId(null);
        record.setStatus("bound");

        // 让实现里的 findBoundRecordByPayment 返回 record
        // 通过 spy 覆盖内部方法
        doReturn(Optional.of(record)).when(referralService).findBoundRecordByPayment(paymentId);
        // 让 grantFreeTrialToReferrer 不实际执行(反射跨模块)
        doReturn(packageId).when(referralService).grantFreeTrialToReferrer(referrerUserId);

        when(referralRecordMapper.updateById(any(ReferralRecordDO.class))).thenReturn(1);

        referralService.triggerReward(paymentId);

        // 标记 rewarded
        verify(referralRecordMapper).updateById(argThat((ReferralRecordDO r) ->
                "rewarded".equals(r.getStatus()) && r.getReferrerRewardPackageId().equals(packageId)
        ));
    }

    /** case 6: 已 rewarded → 幂等 skip */
    @Test
    void triggerReward_alreadyRewarded_idempotent_skip() {
        Long paymentId = 101L;

        ReferralRecordDO record = new ReferralRecordDO();
        record.setId(11L);
        record.setStatus("rewarded"); // 已发放

        doReturn(Optional.of(record)).when(referralService).findBoundRecordByPayment(paymentId);

        referralService.triggerReward(paymentId);

        // 不调 grantFreeTrialToReferrer,不更新状态
        verify(referralService, never()).grantFreeTrialToReferrer(any());
        verify(referralRecordMapper, never()).updateById(any(ReferralRecordDO.class));
    }

    @Test
    void grantFreeTrialToReferrer_usesConfiguredRewardPackage_returnsStudentPackageId() {
        Long referrerUserId = 777L;
        Long packageId = 55L;
        Long studentPackageId = 199L;

        when(configApi.getConfigValueByKey("mandarly.referral.referrer_reward_package_id"))
                .thenReturn(String.valueOf(packageId));
        when(studentPackageService.grantReferralRewardPackage(referrerUserId, packageId))
                .thenReturn(studentPackageId);

        Long result = referralService.grantFreeTrialToReferrer(referrerUserId);

        assertThat(result).isEqualTo(studentPackageId);
    }
}
