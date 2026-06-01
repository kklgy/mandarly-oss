package com.mandarly.boot.module.edu.controller.app.payment;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.app.payment.vo.AppPaymentCheckoutReqVO;
import com.mandarly.boot.module.edu.controller.app.payment.vo.AppPaymentCheckoutRespVO;
import com.mandarly.boot.module.edu.controller.app.payment.vo.AppPaymentRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.payment.PaymentDO;
import com.mandarly.boot.module.edu.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;
import static com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App 端 - 支付下单接口(套餐购买 Flow 1)
 *
 * <p>对应 PRD-v4 §5.2:学生选套餐 → checkout → Stripe 跳转 → webhook 回填。
 * Controller 层为薄层,业务逻辑全在 {@link PaymentService}。
 */
@Tag(name = "用户端 - 支付下单")
@RestController
@RequestMapping("/edu/payment")
@Validated
public class AppPaymentController {

    @Resource
    private PaymentService paymentService;

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "发起套餐购买(返回 Stripe Checkout URL)")
    public CommonResult<AppPaymentCheckoutRespVO> checkout(@Valid @RequestBody AppPaymentCheckoutReqVO req) {
        Long userId = getLoginUserId();
        return success(paymentService.createCheckout(userId, req.getPackageId(), req.getCurrency(), req.getReferralCode()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询单笔支付(success_url 轮询用)")
    @Parameter(name = "id", description = "payment.id", required = true, example = "1")
    public CommonResult<AppPaymentRespVO> get(@PathVariable("id") Long id) {
        PaymentDO payment = paymentService.getMyPayment(getLoginUserId(), id);
        return success(toRespVO(payment));
    }

    @GetMapping("/page")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "分页查询我的支付历史")
    public CommonResult<PageResult<AppPaymentRespVO>> page(@Valid PageParam pageParam) {
        PageResult<PaymentDO> pageResult = paymentService.getMyPaymentPage(getLoginUserId(), pageParam);
        List<AppPaymentRespVO> list = pageResult.getList().stream()
                .map(this::toRespVO)
                .toList();
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    // ======================== private ========================

    private AppPaymentRespVO toRespVO(PaymentDO p) {
        if (p == null) return null;
        AppPaymentRespVO vo = new AppPaymentRespVO();
        vo.setId(p.getId());
        vo.setPackageId(p.getPackageId());
        // packageName 需 JOIN package 表,属于展示层可选增强:此处留 null,前端按 packageId 自取
        vo.setAmountRequest(p.getAmountRequest());
        vo.setAmountPaid(p.getAmountPaid());
        vo.setCurrencyPaid(p.getCurrencyPaid());
        vo.setDiscountAmountUsd(p.getDiscountAmountUsd());
        vo.setStatus(p.getStatus());
        vo.setPaidAt(p.getPaidAt());
        vo.setCreateTime(p.getCreateTime());
        return vo;
    }
}
