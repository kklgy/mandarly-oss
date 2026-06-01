package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.teacher_center.util.PayeeInfoMasker;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppWithdrawalApplyReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppWithdrawalPageReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppWithdrawalRespVO;
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
 * App 端 - 教师中心:提现(M6 §3.7 / §5.4 T4 + §5.5 T5)
 *
 * <p>权限 `@ss.hasRole('teacher')` + teacherId 由 {@link SecurityFrameworkUtils#getLoginUserId()} 取(防越权)。
 *
 * <p><strong>安全</strong>:本端列表 / 详情接口返回的 payee_info 均经
 * {@link PayeeInfoMasker#mask(String, String)} 脱敏(后 4 位),
 * 明文仅 admin Track A7 `reveal-payee` 二次确认后可见。
 */
@Tag(name = "用户端 - 教师中心 - 提现")
@RestController("appTeacherCenterWithdrawalController")
@RequestMapping("/edu/teacher-center/withdrawal")
@Validated
public class AppTeacherWithdrawalController {

    @Resource
    private TeacherWithdrawalService teacherWithdrawalService;

    @PostMapping("/apply")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师申请提现")
    public CommonResult<Long> apply(@Valid @RequestBody AppWithdrawalApplyReqVO reqVO) {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        Long id = teacherWithdrawalService.applyWithdrawal(
                teacherId,
                reqVO.getAmount(),
                reqVO.getPayeeInfo(),
                reqVO.getPayeeMethod());
        return success(id);
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师查自己的提现历史(payee 脱敏)")
    public CommonResult<PageResult<AppWithdrawalRespVO>> list(@Valid AppWithdrawalPageReqVO reqVO) {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<TeacherWithdrawalDO> page = teacherWithdrawalService.getPageForTeacher(
                teacherId, reqVO.getStatus(), reqVO);
        List<AppWithdrawalRespVO> list = page.getList().stream()
                .map(this::toResp)
                .toList();
        return success(new PageResult<>(list, page.getTotal()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "提现详情(payee 脱敏)")
    @Parameter(name = "id", description = "teacher_withdrawal.id", required = true, example = "30001")
    public CommonResult<AppWithdrawalRespVO> get(@PathVariable("id") @Positive Long id) {
        TeacherWithdrawalDO row = teacherWithdrawalService.getDetail(id);
        if (row == null) {
            throw ServiceExceptionUtil.exception0(404, "提现记录不存在");
        }
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        if (!teacherId.equals(row.getTeacherId())) {
            throw ServiceExceptionUtil.exception0(403, "无权查看该提现记录");
        }
        return success(toResp(row));
    }

    private AppWithdrawalRespVO toResp(TeacherWithdrawalDO row) {
        AppWithdrawalRespVO vo = new AppWithdrawalRespVO();
        vo.setId(row.getId());
        vo.setAmount(row.getAmount());
        vo.setCurrency(row.getCurrency());
        vo.setPayeeMethod(row.getPayeeMethod());
        vo.setPayeeInfoMasked(PayeeInfoMasker.mask(row.getPayeeInfo(), row.getPayeeMethod()));
        vo.setStatus(row.getStatus());
        vo.setAppliedAt(row.getAppliedAt());
        vo.setAuditedAt(row.getAuditedAt());
        vo.setRejectReason(row.getRejectReason());
        vo.setPaidAt(row.getPaidAt());
        vo.setPaidRemark(row.getPaidRemark());
        return vo;
    }
}
