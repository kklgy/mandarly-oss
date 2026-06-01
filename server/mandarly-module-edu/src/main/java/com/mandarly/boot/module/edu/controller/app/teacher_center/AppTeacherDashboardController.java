package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherDashboardSummaryRespVO;
import com.mandarly.boot.module.edu.service.teacher.TeacherDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 教师中心:工作台 Dashboard 聚合(D16 P2 / PRD-v4 §4.2 T0)
 *
 * <p>一次返 7 字段(本周课次 / 本月收入 / 待结算 / 累计收入 / 可提现 / 评分均值 / 评分条数),
 * 替代前端 dashboard 同时打 income.balance + schedule.list + review.stat 多次串行。
 *
 * <p>权限走 {@code @ss.hasRole('teacher')}(M6 桥接);userId 由
 * {@link SecurityFrameworkUtils#getLoginUserId()} 兜底取,防越权。
 */
@Tag(name = "用户端 - 教师中心 - 工作台")
@RestController("appTeacherCenterDashboardController")
@RequestMapping("/edu/teacher-center/dashboard")
@Validated
public class AppTeacherDashboardController {

    @Resource
    private TeacherDashboardService teacherDashboardService;

    @GetMapping("/summary")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "教师 - 工作台聚合数据(7 字段:本周课次/本月收入/待结算/累计收入/可提现/评分均值/评分条数)")
    public CommonResult<AppTeacherDashboardSummaryRespVO> getSummary() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(teacherDashboardService.getSummary(userId));
    }
}
