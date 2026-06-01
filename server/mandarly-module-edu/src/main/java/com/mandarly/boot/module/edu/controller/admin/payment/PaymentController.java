package com.mandarly.boot.module.edu.controller.admin.payment;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.module.edu.controller.admin.payment.vo.PaymentPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.payment.vo.PaymentRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.payment.PaymentDO;
import com.mandarly.boot.module.edu.dal.mysql.payment.PaymentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 支付订单接口(只读)
 *
 * <p>对应 PRD-v4 §5.5 admin 支付看板。不暴露 cancel/退款操作(走 RefundController)。
 */
@Tag(name = "管理后台 - 支付订单")
@RestController
@RequestMapping("/edu/payment")
@Validated
public class PaymentController {

    @Resource
    private PaymentMapper paymentMapper;

    @GetMapping("/page")
    @Operation(summary = "支付订单分页查询(含状态 / 用户 / 日期区间过滤)")
    @PreAuthorize("@ss.hasPermission('edu:payment:query')")
    public CommonResult<PageResult<PaymentRespVO>> getPage(@Valid PaymentPageReqVO reqVO) {
        PageResult<PaymentDO> page = paymentMapper.selectPage(reqVO,
                new LambdaQueryWrapperX<PaymentDO>()
                        .eqIfPresent(PaymentDO::getUserId, reqVO.getUserId())
                        .eqIfPresent(PaymentDO::getPackageId, reqVO.getPackageId())
                        .eqIfPresent(PaymentDO::getStatus, reqVO.getStatus())
                        .geIfPresent(PaymentDO::getCreateTime, reqVO.getCreateTimeFrom())
                        .leIfPresent(PaymentDO::getCreateTime, reqVO.getCreateTimeTo())
                        .orderByDesc(PaymentDO::getCreateTime));
        return success(BeanUtils.toBean(page, PaymentRespVO.class));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取支付订单详情(含完整 Stripe 字段)")
    @Parameter(name = "id", description = "payment.id", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('edu:payment:query')")
    public CommonResult<PaymentRespVO> get(@PathVariable("id") Long id) {
        PaymentDO payment = paymentMapper.selectById(id);
        return success(BeanUtils.toBean(payment, PaymentRespVO.class));
    }
}
