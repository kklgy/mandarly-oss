package com.mandarly.boot.module.edu.service.referral;

import cn.hutool.extra.spring.SpringUtil;
import com.mandarly.boot.module.edu.dal.dataobject.payment.PaymentDO;
import com.mandarly.boot.module.edu.dal.dataobject.referral.ReferralRecordDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.payment.PaymentMapper;
import com.mandarly.boot.module.edu.dal.mysql.referral.ReferralRecordMapper;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.enums.referral.ReferralStatusEnum;
import com.mandarly.boot.module.edu.service.booking.StudentPackageService;
import com.mandarly.boot.module.edu.service.mail.PaymentMailTemplateHelper;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import com.mandarly.boot.module.system.api.mail.MailSendApi;
import com.mandarly.boot.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.mandarly.boot.module.edu.controller.app.referral.vo.AppReferralStatsRespVO;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.REFERRAL_CODE_SELF_USE;

/**
 * 推荐码服务实现(Flow 4)
 *
 * <p>关键设计:
 * <ul>
 *   <li>bindAndCalculateDiscount:降级语义(空码/无效/已付/已绑 → 0),唯一抛错路径是自引用</li>
 *   <li>findUserByReferralCode:反射跨模块(沿用 M2.5 模式) → system.UserService.getUserByReferralCode</li>
 *   <li>triggerReward:幂等(status='rewarded' 时 skip)</li>
 *   <li>grantFreeTrialToReferrer:读取 infra_config 奖励套餐并发放 source=referral_reward</li>
 * </ul>
 */
@Slf4j
@Service
public class ReferralServiceImpl implements ReferralService {

    @Resource
    private ReferralRecordMapper referralRecordMapper;

    @Resource
    private PaymentMapper paymentMapper;

    @Resource
    private EduUserMapper eduUserMapper;

    @Resource
    private ConfigApi configApi;

    @Resource
    private StudentPackageService studentPackageService;

    @Resource
    private MailSendApi mailSendApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal bindAndCalculateDiscount(Long refereeUserId, String referralCode) {
        // 1. 查 referrer:优先用 checkout 显式推荐码;为空时兜底使用注册时绑定的 referred_by。
        Optional<ReferralBinding> bindingOpt = resolveReferralBinding(refereeUserId, referralCode);
        if (bindingOpt.isEmpty()) {
            if (referralCode != null && !referralCode.isBlank()) {
                log.info("[bindAndCalculateDiscount] referralCode={} invalid, downgrade to 0", referralCode);
            }
            return BigDecimal.ZERO;
        }
        ReferralBinding binding = bindingOpt.get();
        Long referrerId = binding.referrerUserId();
        String effectiveReferralCode = binding.referralCode();

        // 2. 自引用拦截 — 唯一抛错路径
        if (referrerId.equals(refereeUserId)) {
            throw exception(REFERRAL_CODE_SELF_USE);
        }

        // 3. 首单判断:referee 已有 paid payment → 降级
        if (paymentMapper.existsPaidByUser(refereeUserId)) {
            log.info("[bindAndCalculateDiscount] user={} already paid, downgrade to 0", refereeUserId);
            return BigDecimal.ZERO;
        }

        // 4. referee 已 bound → 降级
        if (referralRecordMapper.selectByReferee(refereeUserId).isPresent()) {
            log.info("[bindAndCalculateDiscount] user={} already bound, downgrade to 0", refereeUserId);
            return BigDecimal.ZERO;
        }

        // 5. 创建 bound 关系 + 算 discount
        BigDecimal discount = new BigDecimal(
                configApi.getConfigValueByKey("mandarly.referral.referee_discount_usd"));

        ReferralRecordDO record = new ReferralRecordDO();
        record.setReferrerUserId(referrerId);
        record.setRefereeUserId(refereeUserId);
        record.setReferralCode(effectiveReferralCode);
        record.setRefereeDiscountAmountUsd(discount);
        record.setStatus(ReferralStatusEnum.BOUND.getCode());
        record.setBoundAt(LocalDateTime.now());
        referralRecordMapper.insert(record);

        return discount;
    }

    private Optional<ReferralBinding> resolveReferralBinding(Long refereeUserId, String referralCode) {
        if (referralCode != null && !referralCode.isBlank()) {
            return findUserByReferralCode(referralCode)
                    .map(referrerId -> new ReferralBinding(referrerId, referralCode));
        }
        UserDO referee = eduUserMapper.selectById(refereeUserId);
        if (referee == null || referee.getReferredBy() == null) {
            return Optional.empty();
        }
        UserDO referrer = eduUserMapper.selectById(referee.getReferredBy());
        if (referrer == null || referrer.getReferralCode() == null || referrer.getReferralCode().isBlank()) {
            log.warn("[resolveReferralBinding] referredBy={} missing referralCode, refereeUserId={}",
                    referee.getReferredBy(), refereeUserId);
            return Optional.empty();
        }
        return Optional.of(new ReferralBinding(referrer.getId(), referrer.getReferralCode()));
    }

    private record ReferralBinding(Long referrerUserId, String referralCode) {}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void attachPayment(Long refereeUserId, Long paymentId) {
        Optional<ReferralRecordDO> opt = referralRecordMapper.selectByReferee(refereeUserId);
        if (opt.isEmpty()) {
            return;
        }
        ReferralRecordDO record = opt.get();
        if (!ReferralStatusEnum.BOUND.getCode().equals(record.getStatus()) || record.getPaymentId() != null) {
            return;
        }
        ReferralRecordDO update = new ReferralRecordDO();
        update.setId(record.getId());
        update.setPaymentId(paymentId);
        referralRecordMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerReward(Long paymentId) {
        Optional<ReferralRecordDO> opt = findBoundRecordByPayment(paymentId);
        if (opt.isEmpty()) {
            log.info("[triggerReward] no referral record for paymentId={}", paymentId);
            return;
        }
        ReferralRecordDO record = opt.get();

        // 幂等:已发放则 skip
        if (ReferralStatusEnum.REWARDED.getCode().equals(record.getStatus())) {
            log.info("[triggerReward] already rewarded, skip. recordId={}", record.getId());
            return;
        }

        // 发放免费体验套餐给 referrer
        Long packageId = grantFreeTrialToReferrer(record.getReferrerUserId());

        // 标记 rewarded
        ReferralRecordDO upd = new ReferralRecordDO();
        upd.setId(record.getId());
        upd.setStatus(ReferralStatusEnum.REWARDED.getCode());
        upd.setRewardedAt(LocalDateTime.now());
        upd.setReferrerRewardPackageId(packageId);
        referralRecordMapper.updateById(upd);

        // Phase 9.2 邮件模板 #4:推荐人获赠免费课通知
        try {
            String referrerLocale = getUserLocale(record.getReferrerUserId());
            String templateCode = PaymentMailTemplateHelper.pickTemplateCode("mandarly_payment_referral_reward", referrerLocale);
            String referrerName = getUserDisplayName(record.getReferrerUserId());
            String refereeEmail = getUserEmail(record.getRefereeUserId());
            Map<String, Object> vars = Map.of(
                    "referrerName", referrerName,
                    "refereeEmail", refereeEmail,
                    PaymentMailTemplateHelper.PARAM_LOCALE, PaymentMailTemplateHelper.normalizeLocale(referrerLocale)
            );
            String referrerEmail = getUserEmail(record.getReferrerUserId());
            if (referrerEmail == null || referrerEmail.isEmpty()) {
                log.warn("[triggerReward] referrer email not found, skip mail. recordId={} referrerUserId={}",
                        record.getId(), record.getReferrerUserId());
            } else {
                MailSendSingleToUserReqDTO req = new MailSendSingleToUserReqDTO();
                req.setToMails(java.util.List.of(referrerEmail));
                req.setTemplateCode(templateCode);
                req.setTemplateParams(vars);
                mailSendApi.sendSingleMailToMember(req);
            }
        } catch (Exception e) {
            log.error("[triggerReward] mail send failed for record {}", record.getId(), e);
        }
        // TODO M5 短信通道
    }

    @Override
    public AppReferralStatsRespVO getMyStats(Long userId) {
        // 1. 取推荐码:反射查 system.UserService.getUser(userId).getReferralCode()
        String referralCode = getUserReferralCode(userId);

        // 2. 统计:referrer_user_id = userId 的所有 referral_record
        int refereeCount = Math.toIntExact(referralRecordMapper.selectCount(
                new com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX<ReferralRecordDO>()
                        .eq(ReferralRecordDO::getReferrerUserId, userId)));
        // 已奖励次数:status = 'rewarded'
        int rewardedCount = referralRecordMapper.countByReferrer(userId);

        return AppReferralStatsRespVO.builder()
                .referralCode(referralCode)
                .refereeCount(refereeCount)
                .rewardedCount(rewardedCount)
                .totalRewardPackages(rewardedCount)
                .build();
    }

    /**
     * 反射取用户 locale,失败降级 "en"。
     */
    private String getUserLocale(Long userId) {
        try {
            Class<?> cls = Class.forName("com.mandarly.boot.module.system.service.user.UserService");
            Object bean = SpringUtil.getBean(cls);
            Method m = cls.getMethod("getById", Long.class);
            Object user = m.invoke(bean, userId);
            if (user == null) return "en";
            Method getLocale = user.getClass().getMethod("getLocale");
            String locale = (String) getLocale.invoke(user);
            return locale != null ? locale : "en";
        } catch (Exception e) {
            log.warn("[getUserLocale] reflection fail for userId={}, fallback en", userId, e);
            return "en";
        }
    }

    /**
     * 反射取用户 email,失败降级空字符串。
     */
    private String getUserEmail(Long userId) {
        try {
            Class<?> cls = Class.forName("com.mandarly.boot.module.system.service.user.UserService");
            Object bean = SpringUtil.getBean(cls);
            Method m = cls.getMethod("getById", Long.class);
            Object user = m.invoke(bean, userId);
            if (user == null) return "";
            Method getEmail = user.getClass().getMethod("getEmail");
            Object email = getEmail.invoke(user);
            return email != null ? email.toString() : "";
        } catch (Exception e) {
            log.warn("[getUserEmail] reflection fail for userId={}", userId, e);
            return "";
        }
    }

    /**
     * 反射取用户 nickname/email 作为显示名,失败降级 "user#" + userId。
     */
    private String getUserDisplayName(Long userId) {
        try {
            Class<?> cls = Class.forName("com.mandarly.boot.module.system.service.user.UserService");
            Object bean = SpringUtil.getBean(cls);
            Method m = cls.getMethod("getById", Long.class);
            Object user = m.invoke(bean, userId);
            if (user == null) return "user#" + userId;
            try {
                Method getNickname = user.getClass().getMethod("getNickname");
                Object nick = getNickname.invoke(user);
                if (nick != null && !nick.toString().isBlank()) return nick.toString();
            } catch (NoSuchMethodException ignored) {}
            Method getEmail = user.getClass().getMethod("getEmail");
            Object email = getEmail.invoke(user);
            return email != null ? email.toString() : "user#" + userId;
        } catch (Exception e) {
            log.warn("[getUserDisplayName] reflection fail for userId={}", userId, e);
            return "user#" + userId;
        }
    }

    /**
     * 反射取 user.referralCode(沿用 M2.5 反射跨模块模式)
     */
    private String getUserReferralCode(Long userId) {
        try {
            Class<?> userServiceClass = Class.forName(
                    "com.mandarly.boot.module.system.service.user.UserService");
            Object bean = SpringUtil.getBean(userServiceClass);
            Method m = userServiceClass.getMethod("getById", Long.class);
            Object user = m.invoke(bean, userId);
            if (user == null) return null;
            Method getReferralCode = user.getClass().getMethod("getReferralCode");
            return (String) getReferralCode.invoke(user);
        } catch (Exception e) {
            log.warn("[getUserReferralCode] reflection fail for userId={}", userId, e);
            return null;
        }
    }

    // ======================== package-private(可被 spy) ========================

    /**
     * 反射跨模块查用户(system.UserService)
     * 独立为 package-private 方法,单元测试通过 spy 覆盖,避免 Spring context 依赖
     */
    Optional<Long> findUserByReferralCode(String code) {
        try {
            Class<?> userServiceClass = Class.forName(
                    "com.mandarly.boot.module.system.service.user.UserService");
            Object bean = SpringUtil.getBean(userServiceClass);
            Method m = userServiceClass.getMethod("getUserByReferralCode", String.class);
            Object result = m.invoke(bean, code);
            if (result == null) return Optional.empty();
            Method getId = result.getClass().getMethod("getId");
            return Optional.ofNullable((Long) getId.invoke(result));
        } catch (Exception e) {
            log.warn("[findUserByReferralCode] reflection fail for code={}", code, e);
            return Optional.empty();
        }
    }

    /**
     * 通过 paymentId 找 referral_record(bound 状态):
     * referral_record 绑定时 payment_id 可能为空(在 bind 时还没有 payment),
     * 需要通过 payment.userId → referralRecordMapper.selectByReferee(userId) 关联
     */
    Optional<ReferralRecordDO> findBoundRecordByPayment(Long paymentId) {
        PaymentDO payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            log.warn("[findBoundRecordByPayment] payment not found: {}", paymentId);
            return Optional.empty();
        }
        return referralRecordMapper.selectByReferee(payment.getUserId());
    }

    /**
     * 发放推荐奖励套餐,并返回 student_package.id。
     */
    Long grantFreeTrialToReferrer(Long referrerUserId) {
        try {
            String packageIdValue = configApi.getConfigValueByKey("mandarly.referral.referrer_reward_package_id");
            Long packageId = Long.valueOf(packageIdValue);
            return studentPackageService.grantReferralRewardPackage(referrerUserId, packageId);
        } catch (Exception e) {
            log.error("[grantFreeTrialToReferrer] reward grant failed for userId={}", referrerUserId, e);
            return null;
        }
    }
}
