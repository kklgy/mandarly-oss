package com.mandarly.boot.module.edu.controller.admin.schedule;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.TeacherScheduleRespVO;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.TeacherScheduleSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.TeacherScheduleDO;
import com.mandarly.boot.module.edu.service.schedule.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 教师周排课")
@RestController
@RequestMapping("/edu/teacher-schedule")
@Validated
public class TeacherScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @GetMapping("/list")
    @Operation(summary = "按教师列出周排课")
    @Parameter(name = "teacherId", description = "教师 user.id", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('edu:teacher-schedule:query')")
    public CommonResult<List<TeacherScheduleRespVO>> listSchedules(@RequestParam("teacherId") Long teacherId) {
        List<TeacherScheduleDO> list = scheduleService.getSchedulesByTeacher(teacherId);
        return success(BeanUtils.toBean(list, TeacherScheduleRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "新增周排课时段")
    @PreAuthorize("@ss.hasPermission('edu:teacher-schedule:create')")
    public CommonResult<Long> createSchedule(@Valid @RequestBody TeacherScheduleSaveReqVO reqVO) {
        return success(scheduleService.createSchedule(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新周排课时段")
    @PreAuthorize("@ss.hasPermission('edu:teacher-schedule:update')")
    public CommonResult<Boolean> updateSchedule(@Valid @RequestBody TeacherScheduleSaveReqVO reqVO) {
        scheduleService.updateSchedule(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除周排课时段")
    @Parameter(name = "id", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('edu:teacher-schedule:delete')")
    public CommonResult<Boolean> deleteSchedule(@RequestParam("id") Long id) {
        scheduleService.deleteSchedule(id);
        return success(true);
    }

}
