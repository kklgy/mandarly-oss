package com.mandarly.boot.module.edu.controller.app.refund;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.app.refund.vo.AppRefundApplyReqVO;
import com.mandarly.boot.module.edu.controller.app.refund.vo.AppRefundRespVO;
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

import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;
import static com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App 端 - 退款申请接口
 *
 * <p>对应 PRD-v4 §5.3:学生申请退款 → 管理员审核(Track 6B) → Stripe 回款 → webhook 更新。
 */
@Tag(name = "用户端 - 退款申请")
@RestController
@RequestMapping("/edu/refund")
@Validated
public class AppRefundController {

    @Resource
    private RefundService refundService;

    @Resource
    private RefundMapper refundMapper;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "申请退款(首次申请,状态机 → pending)")
    public CommonResult<AppRefundRespVO> apply(@Valid @RequestBody AppRefundApplyReqVO req) {
        Long userId = getLoginUserId();
        RefundDO refund = refundService.apply(userId, req.getPaymentId(), req.getReason());
        return success(toRespVO(refund));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "查询单笔退款工单(仅本人可查)")
    @Parameter(name = "id", description = "refund.id", required = true, example = "1")
    public CommonResult<AppRefundRespVO> get(@PathVariable("id") Long id) {
        Long userId = getLoginUserId();
        RefundDO refund = refundMapper.selectById(id);
        if (refund == null || !userId.equals(refund.getUserId())) {
            throw ServiceExceptionUtil.exception0(404, "退款工单不存在或无权查看");
        }
        return success(toRespVO(refund));
    }

    @GetMapping("/page")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "分页查询我的退款记录")
    public CommonResult<PageResult<AppRefundRespVO>> page(@Valid PageParam pageParam) {
        Long userId = getLoginUserId();
        PageResult<RefundDO> pageResult = refundMapper.selectPage(pageParam,
                new com.mandarly.boot.framework.mybatis.core.query.LambdaQueryWrapperX<RefundDO>()
                        .eq(RefundDO::getUserId, userId)
                        .orderByDesc(RefundDO::getCreateTime));
        List<AppRefundRespVO> list = pageResult.getList().stream().map(this::toRespVO).toList();
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    // ======================== private ========================

    private AppRefundRespVO toRespVO(RefundDO r) {
        if (r == null) return null;
        AppRefundRespVO vo = new AppRefundRespVO();
        vo.setId(r.getId());
        vo.setPaymentId(r.getPaymentId());
        vo.setApplyReason(r.getApplyReason());
        vo.setSuggestedAmountUsd(r.getSuggestedAmountUsd());
        vo.setFinalAmountUsd(r.getFinalAmountUsd());
        vo.setStatus(r.getStatus());
        vo.setAuditNote(r.getAuditNote());
        vo.setRefundedAt(r.getRefundedAt());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }
}
