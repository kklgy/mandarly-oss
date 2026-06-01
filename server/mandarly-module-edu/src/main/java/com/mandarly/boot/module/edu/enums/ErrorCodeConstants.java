package com.mandarly.boot.module.edu.enums;

import com.mandarly.boot.framework.common.exception.ErrorCode;

/**
 * Edu 模块错误码段
 *
 * 段位约定(M4 起):
 * - 1_004_000 ~ 1_004_099 通用 / 套餐 / 推荐码
 * - 1_004_100 ~ 1_004_199 支付 payment
 * - 1_004_200 ~ 1_004_299 退款 refund
 * - 1_004_300 ~ 1_004_399 教师收入 income / balance
 * - 1_004_400 ~ 1_004_499 webhook
 * - 1_004_500 ~ 1_004_599 提现 withdrawal(M6)
 * - 1_004_600 ~ 1_004_699 客服 support
 */
public interface ErrorCodeConstants {

    // ========== 推荐码 (1_004_001~009) ==========
    ErrorCode REFERRAL_CODE_INVALID         = new ErrorCode(1_004_001, "推荐码无效");
    ErrorCode REFERRAL_CODE_SELF_USE        = new ErrorCode(1_004_002, "推荐码不能引用自己");
    ErrorCode REFERRAL_ALREADY_BOUND        = new ErrorCode(1_004_003, "已被推荐过,不能再用推荐码");

    // ========== 套餐 + 购买 (1_004_010~019) ==========
    ErrorCode PACKAGE_NOT_FOUND             = new ErrorCode(1_004_010, "套餐不存在或已下架");
    ErrorCode PAYMENT_DUPLICATE_PENDING     = new ErrorCode(1_004_011, "您有进行中的支付订单,请稍候再试");
    ErrorCode BOOKING_SLOT_LOCK_TIMEOUT     = new ErrorCode(1_004_012, "该时段正在被预约,请稍后重试");

    // ========== 支付 payment (1_004_020~099) ==========
    ErrorCode PAYMENT_NOT_FOUND             = new ErrorCode(1_004_020, "支付订单不存在或不属于您");
    ErrorCode PAYMENT_STATUS_INVALID        = new ErrorCode(1_004_021, "支付订单状态不允许此操作");

    // ========== 退款 refund (1_004_200~299) ==========
    ErrorCode REFUND_NOT_FOUND              = new ErrorCode(1_004_200, "退款工单不存在");
    ErrorCode REFUND_AMOUNT_EXCEEDS         = new ErrorCode(1_004_201, "退款金额超过原支付金额");
    ErrorCode REFUND_ADJUST_REASON_REQUIRED = new ErrorCode(1_004_202, "调整退款金额必须填写原因");
    ErrorCode REFUND_DUPLICATE_PENDING      = new ErrorCode(1_004_203, "该订单已有进行中的退款工单");
    ErrorCode REFUND_STATUS_INVALID         = new ErrorCode(1_004_204, "退款工单状态不允许此操作");

    // ========== 教师收入 income / balance (1_004_300~399) ==========
    ErrorCode INCOME_NOT_FOUND              = new ErrorCode(1_004_300, "教师收入流水不存在");
    ErrorCode INCOME_ALREADY_REVERTED       = new ErrorCode(1_004_301, "该笔收入已被退款扣回,不可重复操作");
    ErrorCode BALANCE_VERSION_CONFLICT      = new ErrorCode(1_004_302, "余额并发冲突,请重试");
    ErrorCode INCOME_LOCK_TIMEOUT           = new ErrorCode(1_004_303, "余额操作并发冲突,请重试");
    ErrorCode INCOME_STATUS_INVALID         = new ErrorCode(1_004_304, "教师收入状态异常,无法扣回");

    // ========== Stripe 通道 (1_004_400~499) ==========
    ErrorCode STRIPE_CHANNEL_ERROR          = new ErrorCode(1_004_400, "支付通道异常,请稍后重试");
    ErrorCode STRIPE_WEBHOOK_SIG_INVALID    = new ErrorCode(1_004_401, "Webhook 签名验证失败");

    // ========== 提现 withdrawal (1_004_500~599) ==========
    ErrorCode WITHDRAWAL_NOT_FOUND              = new ErrorCode(1_004_500, "提现申请不存在");
    ErrorCode WITHDRAWAL_PENDING_EXISTS         = new ErrorCode(1_004_501, "您已有进行中的提现申请,请等待审核完成");
    ErrorCode WITHDRAWAL_BELOW_MIN              = new ErrorCode(1_004_502, "提现金额低于最低额度 {}");
    ErrorCode WITHDRAWAL_INVALID_STATUS         = new ErrorCode(1_004_503, "提现状态不允许此操作");
    ErrorCode WITHDRAWAL_REJECT_REASON_REQUIRED = new ErrorCode(1_004_504, "驳回必须填写原因");
    ErrorCode WITHDRAWAL_FAIL_REASON_REQUIRED   = new ErrorCode(1_004_505, "打款失败必须填写原因");
    ErrorCode WITHDRAWAL_LOCK_TIMEOUT           = new ErrorCode(1_004_506, "提现操作并发冲突,请重试");

    // ========== 客服 support (1_004_600~699) ==========
    ErrorCode SUPPORT_RATE_LIMIT                 = new ErrorCode(1_004_600, "提问过于频繁,请稍后再试");

    // ========== 教师 teacher (1_004_700~799) ==========
    ErrorCode TEACHER_NOT_APPROVED                       = new ErrorCode(1_004_700, "教师未通过审核");
    ErrorCode TEACHER_PROFILE_INVALID_STATUS_FOR_SUBMIT  = new ErrorCode(1_004_701, "当前状态不允许提交审核");
    ErrorCode TEACHER_REQUIRED_QUALIFICATION_MISSING     = new ErrorCode(1_004_702, "请先上传身份证件和学历证书后再提交审核");
}
