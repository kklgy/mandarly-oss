package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.ScheduleExceptionRespVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherScheduleExceptionReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherScheduleToggleReqVO;
import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherScheduleWeeklyRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.ScheduleExceptionDO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.TeacherScheduleDO;
import com.mandarly.boot.module.edu.service.schedule.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 教师中心:排课设置(M6 §3.7 / §5.1 T1)
 *
 * <p>权限:`@ss.hasRole('teacher')`(ruoyi 风格 SpEL)+ 兜底用 {@link SecurityFrameworkUtils#getLoginUserId()}
 * 取当前教师 id,<strong>不接收</strong>路径 / body 中的 teacherId 参数(防越权)。
 *
 * <p>注:与既有 {@code controller/app/schedule/AppTeacherScheduleController}(公开 / 学生查询用)
 * 类名相同,显式给定 Bean name 避免 Spring AnnotationBeanNameGenerator 冲突。
 */
@Tag(name = "用户端 - 教师中心 - 排课")
@RestController("appTeacherCenterScheduleController")
@RequestMapping("/edu/teacher-center/schedule")
@Validated
public class AppTeacherScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @GetMapping("/weekly")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "查教师本人的周排课模板(7 天 × 时段)")
    public CommonResult<AppTeacherScheduleWeeklyRespVO> getWeekly() {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        List<TeacherScheduleDO> rows = scheduleService.getSchedulesByTeacher(teacherId);

        AppTeacherScheduleWeeklyRespVO resp = new AppTeacherScheduleWeeklyRespVO();
        resp.setTeacherId(teacherId);
        if (rows != null && !rows.isEmpty()) {
            // 首条 timezone 作 PC/H5 渲染基准(同教师所有周排课通常一致)
            resp.setTimezone(rows.get(0).getTimezone());
        }
        List<AppTeacherScheduleWeeklyRespVO.Slot> slots = (rows == null) ? List.of() : rows.stream()
                .map(r -> {
                    AppTeacherScheduleWeeklyRespVO.Slot s = new AppTeacherScheduleWeeklyRespVO.Slot();
                    s.setId(r.getId());
                    s.setWeekday(r.getWeekday());
                    s.setStartTime(r.getStartTime());
                    s.setEndTime(r.getEndTime());
                    return s;
                })
                .toList();
        resp.setSlots(slots);
        return success(resp);
    }

    @PostMapping("/toggle")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "切换周模板单格子(30-min)— available=true 新增 / false 撤销")
    public CommonResult<Long> toggleWeekly(@Valid @RequestBody AppTeacherScheduleToggleReqVO reqVO) {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        Long id = scheduleService.toggleWeeklySlot(teacherId,
                reqVO.getDayOfWeek(), reqVO.getHh(), reqVO.getMm(),
                reqVO.getAvailable(), reqVO.getTimezone());
        return success(id);
    }

    @GetMapping("/exceptions")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "查教师本人单周例外(from/to 可空,空时返回全部)")
    public CommonResult<List<ScheduleExceptionRespVO>> getExceptions(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        List<ScheduleExceptionDO> rows = scheduleService.getExceptions(teacherId, from, to);
        List<ScheduleExceptionRespVO> vos = (rows == null) ? List.of() : rows.stream()
                .map(r -> {
                    ScheduleExceptionRespVO vo = new ScheduleExceptionRespVO();
                    vo.setId(r.getId());
                    vo.setTeacherId(r.getTeacherId());
                    vo.setExceptionDate(r.getExceptionDate());
                    vo.setType(r.getType());
                    vo.setStartTime(r.getStartTime());
                    vo.setEndTime(r.getEndTime());
                    vo.setTimezone(r.getTimezone());
                    vo.setReason(r.getReason());
                    vo.setCreateTime(r.getCreateTime());
                    return vo;
                })
                .toList();
        return success(vos);
    }

    @PostMapping("/exceptions")
    @PreAuthorize("@ss.hasRole('teacher')")
    @Operation(summary = "添加 / 删除单次例外(action=closed/extra/remove)")
    public CommonResult<Long> toggleException(@Valid @RequestBody AppTeacherScheduleExceptionReqVO reqVO) {
        Long teacherId = SecurityFrameworkUtils.getLoginUserId();
        Long id = scheduleService.toggleException(teacherId,
                reqVO.getDate(), reqVO.getHh(), reqVO.getMm(),
                reqVO.getAction(), reqVO.getTimezone(), reqVO.getReason());
        return success(id);
    }

}
