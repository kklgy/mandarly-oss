package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherBalanceRespVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherIncomePageReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherIncomeRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherBalanceDO;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherIncomeDO;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 教师中心:收入(M6 §3.7 / §5.3 T3)
 *
 * <p>balance:5 字段(available / frozen_t7 / pending_withdraw / total_earned / total_withdrawn)。
 * <p>list:teacher_income 流水分页,按 settledAt 倒序,可选 from/to/type/status 过滤。
 *
 * <p>权限 `@ss.hasRole('teacher')` + teacherId 由 {@link SecurityFrameworkUtils#getLoginUserId()} 取(防越权)。
 */
@Tag(name = "用户端 - 教师中心 - 收入")
@RestController("appTeacherCenterIncomeController")
@RequestMapping("/edu/teacher-center/income")
@Validated
public class AppTeacherIncomeController {

    @Resource
    private TeacherBalanceService teacherBalanceService;

    @Resource
    private TeacherIncomeMapper teacherIncomeMapper;

    @GetMapping("/balance")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师余额(5 字段:available / frozen_t7 / pending_withdraw / total_earned / total_withdrawn)")
    public CommonResult<AppTeacherBalanceRespVO> getBalance() {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        TeacherBalanceDO balance = teacherBalanceService.getBalance(teacherId);

        AppTeacherBalanceRespVO vo = new AppTeacherBalanceRespVO();
        if (balance == null) {
            // 新教师无入账 → 5 字段全 0 USD
            vo.setAvailableUsd(BigDecimal.ZERO);
            vo.setFrozenT7Usd(BigDecimal.ZERO);
            vo.setPendingWithdrawUsd(BigDecimal.ZERO);
            vo.setTotalEarnedUsd(BigDecimal.ZERO);
            vo.setTotalWithdrawnUsd(BigDecimal.ZERO);
            vo.setCurrency("USD");
        } else {
            vo.setAvailableUsd(balance.getAvailableUsd());
            vo.setFrozenT7Usd(balance.getFrozenT7Usd());
            vo.setPendingWithdrawUsd(balance.getPendingWithdrawUsd());
            vo.setTotalEarnedUsd(balance.getTotalEarnedUsd());
            vo.setTotalWithdrawnUsd(balance.getTotalWithdrawnUsd());
            vo.setCurrency(balance.getCurrency() == null ? "USD" : balance.getCurrency());
        }
        return success(vo);
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师收入流水分页(按 settledAt 倒序,可选 from/to/type/status)")
    public CommonResult<PageResult<AppTeacherIncomeRespVO>> list(@Valid AppTeacherIncomePageReqVO reqVO) {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<TeacherIncomeDO> page = teacherIncomeMapper.selectMyIncomePage(
                teacherId,
                reqVO.getFrom(), reqVO.getTo(),
                reqVO.getType(), reqVO.getStatus(),
                reqVO);

        List<AppTeacherIncomeRespVO> list = BeanUtils.toBean(page.getList(), AppTeacherIncomeRespVO.class);
        return success(new PageResult<>(list, page.getTotal()));
    }
}
