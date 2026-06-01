package com.mandarly.boot.module.edu.controller.app.schedule;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.ScheduleExceptionRespVO;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.TeacherScheduleRespVO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.ScheduleExceptionDO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.TeacherScheduleDO;
import com.mandarly.boot.module.edu.service.schedule.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 教师排课公开查询
 *
 * <p>学生在选教师 / 看可约日期前会查这两个接口,信息为公开内容(不含敏感字段),无需登录。
 * <p>"某日某时区下的可约时段计算"由 booking 子域负责,本接口仅返回原始数据。
 */
@Tag(name = "用户端 - 教师排课(公开)")
@RestController
@RequestMapping("/edu/teacher-schedule")
@Validated
public class AppTeacherScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @GetMapping("/list")
    @PermitAll
    @Operation(summary = "查教师周排课")
    @Parameter(name = "teacherId", description = "教师 user.id", required = true, example = "1024")
    public CommonResult<List<TeacherScheduleRespVO>> listSchedules(@RequestParam("teacherId") Long teacherId) {
        List<TeacherScheduleDO> list = scheduleService.getSchedulesByTeacher(teacherId);
        return success(BeanUtils.toBean(list, TeacherScheduleRespVO.class));
    }

    @GetMapping("/exceptions")
    @PermitAll
    @Operation(summary = "查教师排课例外(可指定日期范围)")
    public CommonResult<List<ScheduleExceptionRespVO>> listExceptions(
            @RequestParam("teacherId") Long teacherId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<ScheduleExceptionDO> list = scheduleService.getExceptions(teacherId, from, to);
        return success(BeanUtils.toBean(list, ScheduleExceptionRespVO.class));
    }

}
