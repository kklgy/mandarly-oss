package com.mandarly.boot.module.edu.controller.admin.withdrawal;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.admin.withdrawal.vo.WithdrawalAuditReqVO;
import com.mandarly.boot.module.edu.controller.admin.withdrawal.vo.WithdrawalMarkFailedReqVO;
import com.mandarly.boot.module.edu.controller.admin.withdrawal.vo.WithdrawalMarkPaidReqVO;
import com.mandarly.boot.module.edu.controller.admin.withdrawal.vo.WithdrawalPageReqVO;
import com.mandarly.boot.module.edu.controller.admin.withdrawal.vo.WithdrawalRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.withdrawal.TeacherWithdrawalDO;
import com.mandarly.boot.module.edu.service.withdrawal.TeacherWithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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

/**
 * 管理后台 - 教师提现审核 / 打款(M6 §6.4)
 *
 * <p>路径前缀 {@code /admin-api/edu/withdrawal}(全局前缀由网关 / SecurityConfig 处理)。
 *
 * <p>权限:走 ruoyi {@code @PreAuthorize("@ss.hasPermission('edu:withdrawal:...')")} 风格,
 * 与 admin 端菜单授权配置对齐:
 * <ul>
 *   <li>{@code edu:withdrawal:query} — 列表 / 详情</li>
 *   <li>{@code edu:withdrawal:audit} — 审核(approve/reject)</li>
 *   <li>{@code edu:withdrawal:pay} — 标记打款成功 / 失败</li>
 *   <li>{@code edu:withdrawal:reveal-payee} — 查看完整收款信息明文(独立权限,二次确认)</li>
 * </ul>
 *
 * <p><strong>安全</strong>:列表 + 详情返回的 payee_info 均经
 * {@link com.mandarly.boot.module.edu.controller.app.teacher_center.util.PayeeInfoMasker} 脱敏;
 * 仅 {@code GET /{id}/reveal-payee} 返回明文,且 Service 内写一次性审计日志(spec §6.5)。
 */
@Tag(name = "管理后台 - 教师提现")
@RestController
@RequestMapping("/edu/withdrawal")
@Validated
public class TeacherWithdrawalController {

    @Resource
    private TeacherWithdrawalService teacherWithdrawalService;

    @GetMapping("/page")
    @Operation(summary = "提现申请分页(payee 脱敏)")
    @PreAuthorize("@ss.hasPermission('edu:withdrawal:query')")
    public CommonResult<PageResult<WithdrawalRespVO>> getPage(@Valid WithdrawalPageReqVO reqVO) {
        PageResult<TeacherWithdrawalDO> page = teacherWithdrawalService.getPageForAdmin(reqVO);
        List<WithdrawalRespVO> list = page.getList().stream()
                .map(WithdrawalRespVO::fromDO)
                .toList();
        return success(new PageResult<>(list, page.getTotal()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "提现申请详情(payee 脱敏)")
    @Parameter(name = "id", description = "teacher_withdrawal.id", required = true, example = "30001")
    @PreAuthorize("@ss.hasPermission('edu:withdrawal:query')")
    public CommonResult<WithdrawalRespVO> get(@PathVariable("id") @Positive Long id) {
        TeacherWithdrawalDO row = teacherWithdrawalService.getDetail(id);
        if (row == null) {
            throw ServiceExceptionUtil.exception0(404, "提现记录不存在");
        }
        return success(WithdrawalRespVO.fromDO(row));
    }

    @PostMapping("/{id}/audit")
    @Operation(summary = "审核:approved=true → approved(不动余额);approved=false → rejected(余额回退)")
    @Parameter(name = "id", description = "teacher_withdrawal.id", required = true, example = "30001")
    @PreAuthorize("@ss.hasPermission('edu:withdrawal:audit')")
    public CommonResult<Boolean> audit(
            @PathVariable("id") @Positive Long id,
            @Valid @RequestBody WithdrawalAuditReqVO req) {
        Long adminId = SecurityFrameworkUtils.getLoginUserId();
        teacherWithdrawalService.audit(id, Boolean.TRUE.equals(req.getApproved()), req.getRejectReason(), adminId);
        return success(true);
    }

    @PostMapping("/{id}/mark-paid")
    @Operation(summary = "标记打款成功(approved → paid)")
    @Parameter(name = "id", description = "teacher_withdrawal.id", required = true, example = "30001")
    @PreAuthorize("@ss.hasPermission('edu:withdrawal:pay')")
    public CommonResult<Boolean> markPaid(
            @PathVariable("id") @Positive Long id,
            @Valid @RequestBody WithdrawalMarkPaidReqVO req) {
        Long adminId = SecurityFrameworkUtils.getLoginUserId();
        teacherWithdrawalService.markPaid(id, req.getPaidProof(), req.getPaidRemark(), adminId);
        return success(true);
    }

    @PostMapping("/{id}/mark-failed")
    @Operation(summary = "标记打款失败(approved → failed,余额回退 + 必填 failReason)")
    @Parameter(name = "id", description = "teacher_withdrawal.id", required = true, example = "30001")
    @PreAuthorize("@ss.hasPermission('edu:withdrawal:pay')")
    public CommonResult<Boolean> markFailed(
            @PathVariable("id") @Positive Long id,
            @Valid @RequestBody WithdrawalMarkFailedReqVO req) {
        Long adminId = SecurityFrameworkUtils.getLoginUserId();
        teacherWithdrawalService.markFailed(id, req.getFailReason(), adminId);
        return success(true);
    }

    @GetMapping("/{id}/reveal-payee")
    @Operation(summary = "查看完整收款信息明文(二次确认 + 一次性审计日志)")
    @Parameter(name = "id", description = "teacher_withdrawal.id", required = true, example = "30001")
    @PreAuthorize("@ss.hasPermission('edu:withdrawal:reveal-payee')")
    public CommonResult<String> revealPayee(@PathVariable("id") @Positive Long id) {
        Long adminId = SecurityFrameworkUtils.getLoginUserId();
        String payeeInfo = teacherWithdrawalService.getUnmaskedPayeeInfo(id, adminId);
        return success(payeeInfo);
    }
}
