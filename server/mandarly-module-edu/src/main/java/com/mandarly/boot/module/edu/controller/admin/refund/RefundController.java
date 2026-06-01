package com.mandarly.boot.module.edu.controller.admin.refund;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.admin.refund.vo.RefundApproveReqVO;
import com.mandarly.boot.module.edu.controller.admin.refund.vo.RefundPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.refund.vo.RefundRejectReqVO;
import com.mandarly.boot.module.edu.controller.admin.refund.vo.RefundRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.payment.RefundDO;
import com.mandarly.boot.module.edu.dal.mysql.payment.RefundMapper;
import com.mandarly.boot.module.edu.service.refund.RefundService;
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

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 退款工单接口(审核)
 *
 * <p>对应 PRD-v4 §5.3 admin 退款审核流程:approve 调 Stripe / reject 仅标记数据库。
 */
@Tag(name = "管理后台 - 退款工单")
@RestController
@RequestMapping("/edu/refund")
@Validated
public class RefundController {

    @Resource
    private RefundService refundService;

    @Resource
    private RefundMapper refundMapper;

    @GetMapping("/page")
    @Operation(summary = "退款工单分页查询(含状态 / 用户 / 支付单过滤)")
    @PreAuthorize("@ss.hasPermission('edu:refund:query')")
    public CommonResult<PageResult<RefundRespVO>> getPage(@Valid RefundPageReqVO reqVO) {
        PageResult<RefundDO> page = refundMapper.selectPage(reqVO,
                new LambdaQueryWrapperX<RefundDO>()
                        .eqIfPresent(RefundDO::getUserId, reqVO.getUserId())
                        .eqIfPresent(RefundDO::getPaymentId, reqVO.getPaymentId())
                        .eqIfPresent(RefundDO::getStatus, reqVO.getStatus())
                        .orderByDesc(RefundDO::getCreateTime));
        return success(BeanUtils.toBean(page, RefundRespVO.class));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取退款工单详情")
    @Parameter(name = "id", description = "refund.id", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('edu:refund:query')")
    public CommonResult<RefundRespVO> get(@PathVariable("id") Long id) {
        RefundDO refund = refundMapper.selectById(id);
        return success(BeanUtils.toBean(refund, RefundRespVO.class));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过退款(调 Stripe 退款 + 扣回教师收入)")
    @Parameter(name = "id", description = "refund.id", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('edu:refund:approve')")
    public CommonResult<Boolean> approve(
            @PathVariable("id") Long id,
            @Valid @RequestBody RefundApproveReqVO req) {
        Long adminId = SecurityFrameworkUtils.getLoginUserId();
        refundService.approve(adminId, id, req.getFinalAmountUsd(), req.getAdjustReason(), req.getAuditNote());
        return success(true);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝退款申请(仅标记数据库,不调 Stripe)")
    @Parameter(name = "id", description = "refund.id", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('edu:refund:approve')")
    public CommonResult<Boolean> reject(
            @PathVariable("id") Long id,
            @Valid @RequestBody RefundRejectReqVO req) {
        Long adminId = SecurityFrameworkUtils.getLoginUserId();
        refundService.reject(adminId, id, req.getAuditNote());
        return success(true);
    }
}
