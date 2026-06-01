package com.mandarly.boot.module.edu.controller.admin.schedule;

import com.mandarly.boot.framework.common.pojo.CommonResult;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.ScheduleExceptionRespVO;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.ScheduleExceptionSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.ScheduleExceptionDO;
import com.mandarly.boot.module.edu.service.schedule.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.mandarly.boot.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 教师排课例外")
@RestController
@RequestMapping("/edu/schedule-exception")
@Validated
public class ScheduleExceptionController {

    @Resource
    private ScheduleService scheduleService;

    @GetMapping("/list")
    @Operation(summary = "按教师 + 日期范围列出例外记录")
    @PreAuthorize("@ss.hasPermission('edu:schedule-exception:query')")
    public CommonResult<List<ScheduleExceptionRespVO>> listExceptions(
            @RequestParam("teacherId") Long teacherId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<ScheduleExceptionDO> list = scheduleService.getExceptions(teacherId, from, to);
        return success(BeanUtils.toBean(list, ScheduleExceptionRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "新增排课例外(closed/extra)")
    @PreAuthorize("@ss.hasPermission('edu:schedule-exception:create')")
    public CommonResult<Long> createException(@Valid @RequestBody ScheduleExceptionSaveReqVO reqVO) {
        return success(scheduleService.createException(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除排课例外")
    @Parameter(name = "id", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('edu:schedule-exception:delete')")
    public CommonResult<Boolean> deleteException(@RequestParam("id") Long id) {
        scheduleService.deleteException(id);
        return success(true);
    }

}
