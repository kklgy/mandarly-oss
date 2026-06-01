package com.mandarly.boot.module.edu.service.schedule;

import com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil;
import com.mandarly.boot.framework.common.util.object.BeanUtils;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.ScheduleExceptionSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.TeacherScheduleSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.ScheduleExceptionDO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.TeacherScheduleDO;
import com.mandarly.boot.module.edu.dal.dataobject.teacher.TeacherProfileDO;
import com.mandarly.boot.module.edu.dal.mysql.schedule.ScheduleExceptionMapper;
import com.mandarly.boot.module.edu.dal.mysql.schedule.TeacherScheduleMapper;
import com.mandarly.boot.module.edu.dal.mysql.teacher.TeacherProfileMapper;
import com.mandarly.boot.module.edu.enums.teacher.TeacherAuditStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static com.mandarly.boot.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.mandarly.boot.module.edu.enums.ErrorCodeConstants.TEACHER_NOT_APPROVED;

@Service
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    @Resource
    private TeacherScheduleMapper teacherScheduleMapper;

    @Resource
    private ScheduleExceptionMapper scheduleExceptionMapper;

    @Resource
    private TeacherProfileMapper teacherProfileMapper;

    // ========== teacher_schedule ==========

    @Override
    public List<TeacherScheduleDO> getSchedulesByTeacher(Long teacherId) {
        return teacherScheduleMapper.selectListByTeacherId(teacherId);
    }

    @Override
    public Long createSchedule(TeacherScheduleSaveReqVO reqVO) {
        validateTimezone(reqVO.getTimezone());
        validateTimeRange(reqVO.getStartTime(), reqVO.getEndTime());
        TeacherScheduleDO entity = BeanUtils.toBean(reqVO, TeacherScheduleDO.class);
        teacherScheduleMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateSchedule(TeacherScheduleSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "更新时 id 必填");
        }
        if (teacherScheduleMapper.selectById(reqVO.getId()) == null) {
            throw ServiceExceptionUtil.exception0(404, "排课不存在");
        }
        validateTimezone(reqVO.getTimezone());
        validateTimeRange(reqVO.getStartTime(), reqVO.getEndTime());
        teacherScheduleMapper.updateById(BeanUtils.toBean(reqVO, TeacherScheduleDO.class));
    }

    @Override
    public void deleteSchedule(Long id) {
        if (teacherScheduleMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception0(404, "排课不存在");
        }
        teacherScheduleMapper.deleteById(id);
    }

    // ========== schedule_exception ==========

    @Override
    public List<ScheduleExceptionDO> getExceptions(Long teacherId, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw ServiceExceptionUtil.exception0(400, "from 不能晚于 to");
        }
        return scheduleExceptionMapper.selectListByTeacherIdAndDateRange(teacherId, from, to);
    }

    @Override
    public Long createException(ScheduleExceptionSaveReqVO reqVO) {
        validateTimezone(reqVO.getTimezone());
        validateExceptionTimeRange(reqVO);
        ScheduleExceptionDO entity = BeanUtils.toBean(reqVO, ScheduleExceptionDO.class);
        scheduleExceptionMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void deleteException(Long id) {
        if (scheduleExceptionMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception0(404, "例外记录不存在");
        }
        scheduleExceptionMapper.deleteById(id);
    }

    // ========== M6 §3.7 A6:教师 app 周模板 / 例外切换 ==========

    /** 一期固定 30 分钟格子 */
    private static final int SLOT_MINUTES = 30;

    @Override
    public Long toggleWeeklySlot(Long teacherId, Integer weekday, Integer hh, Integer mm,
                                 Boolean available, String timezone) {
        if (teacherId == null || weekday == null || hh == null || mm == null || available == null) {
            throw ServiceExceptionUtil.exception0(400, "toggle 参数不完整");
        }
        // D19 A4:闭合 backdoor — pending/rejected 教师不能配排课
        assertTeacherApproved(teacherId);
        if (weekday < 0 || weekday > 6) {
            throw ServiceExceptionUtil.exception0(400, "weekday 范围 0-6");
        }
        if (mm != 0 && mm != 30) {
            throw ServiceExceptionUtil.exception0(400, "mm 必须为 0 或 30(一期固定 30-min 网格)");
        }
        LocalTime start = LocalTime.of(hh, mm);
        LocalTime end = start.plusMinutes(SLOT_MINUTES);

        List<TeacherScheduleDO> existing = teacherScheduleMapper.selectListByTeacherId(teacherId);
        TeacherScheduleDO exactMatch = existing.stream()
                .filter(r -> r.getWeekday().equals(weekday)
                        && start.equals(r.getStartTime())
                        && end.equals(r.getEndTime()))
                .findFirst().orElse(null);

        if (Boolean.TRUE.equals(available)) {
            if (exactMatch != null) {
                return null; // 幂等
            }
            validateTimezone(timezone);
            TeacherScheduleDO row = new TeacherScheduleDO();
            row.setTeacherId(teacherId);
            row.setWeekday(weekday);
            row.setStartTime(start);
            row.setEndTime(end);
            row.setTimezone(timezone);
            teacherScheduleMapper.insert(row);
            return row.getId();
        } else {
            if (exactMatch == null) {
                // 仅有覆盖该格子的更大区间 → 由前端走详细编辑(不在 toggle 范围内拆分)
                return null;
            }
            teacherScheduleMapper.deleteById(exactMatch.getId());
            return null;
        }
    }

    @Override
    public Long toggleException(Long teacherId, LocalDate date, Integer hh, Integer mm,
                                String action, String timezone, String reason) {
        if (teacherId == null || date == null || hh == null || mm == null || action == null) {
            throw ServiceExceptionUtil.exception0(400, "exception 参数不完整");
        }
        // D19 A4:闭合 backdoor — pending/rejected 教师不能配排课例外
        assertTeacherApproved(teacherId);
        if (mm != 0 && mm != 30) {
            throw ServiceExceptionUtil.exception0(400, "mm 必须为 0 或 30");
        }
        LocalTime start = LocalTime.of(hh, mm);
        LocalTime end = start.plusMinutes(SLOT_MINUTES);

        List<ScheduleExceptionDO> existing = scheduleExceptionMapper
                .selectListByTeacherIdAndDateRange(teacherId, date, date);

        if ("remove".equals(action)) {
            // 精确匹配 (date, hh:mm) 起点的所有例外
            for (ScheduleExceptionDO ex : existing) {
                if (start.equals(ex.getStartTime()) && end.equals(ex.getEndTime())) {
                    scheduleExceptionMapper.deleteById(ex.getId());
                }
            }
            return null;
        }

        if (!"closed".equals(action) && !"extra".equals(action)) {
            throw ServiceExceptionUtil.exception0(400, "action 仅支持 closed/extra/remove");
        }
        validateTimezone(timezone);

        // 幂等检测:已存在同 (start, end, type) 的例外 → 跳过
        for (ScheduleExceptionDO ex : existing) {
            if (action.equals(ex.getType())
                    && start.equals(ex.getStartTime())
                    && end.equals(ex.getEndTime())) {
                return null;
            }
        }
        ScheduleExceptionDO row = new ScheduleExceptionDO();
        row.setTeacherId(teacherId);
        row.setExceptionDate(date);
        row.setType(action);
        row.setStartTime(start);
        row.setEndTime(end);
        row.setTimezone(timezone);
        row.setReason(reason);
        scheduleExceptionMapper.insert(row);
        return row.getId();
    }

    // ========== 校验 ==========

    /**
     * D19 A4:闭合 backdoor — 仅 audit_status=approved 教师可配排课/例外。
     */
    private void assertTeacherApproved(Long teacherId) {
        TeacherProfileDO p = teacherProfileMapper.selectById(teacherId);
        if (p == null
                || !TeacherAuditStatusEnum.APPROVED.getCode().equals(p.getAuditStatus())) {
            throw exception(TEACHER_NOT_APPROVED);
        }
    }

    private void validateTimezone(String tz) {
        try {
            ZoneId.of(tz);
        } catch (Exception e) {
            throw ServiceExceptionUtil.exception0(400, "timezone 不是合法 IANA 时区:" + tz);
        }
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw ServiceExceptionUtil.exception0(400, "endTime 必须晚于 startTime");
        }
    }

    private void validateExceptionTimeRange(ScheduleExceptionSaveReqVO reqVO) {
        boolean closed = "closed".equals(reqVO.getType());
        boolean extra = "extra".equals(reqVO.getType());
        if (extra) {
            if (reqVO.getStartTime() == null || reqVO.getEndTime() == null) {
                throw ServiceExceptionUtil.exception0(400, "extra 类型必须填 startTime / endTime");
            }
            validateTimeRange(reqVO.getStartTime(), reqVO.getEndTime());
        } else if (closed) {
            // closed 允许 start/end 同时为空(整天关闭),也允许同时填写(关闭一段时间)
            if ((reqVO.getStartTime() == null) != (reqVO.getEndTime() == null)) {
                throw ServiceExceptionUtil.exception0(400, "closed 时 startTime / endTime 必须同时为空或同时填写");
            }
            if (reqVO.getStartTime() != null) {
                validateTimeRange(reqVO.getStartTime(), reqVO.getEndTime());
            }
        }
    }

}
