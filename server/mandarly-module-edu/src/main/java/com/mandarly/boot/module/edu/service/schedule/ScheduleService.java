package com.mandarly.boot.module.edu.service.schedule;

import com.mandarly.boot.module.edu.controller.admin.schedule.vo.ScheduleExceptionSaveReqVO;
import com.mandarly.boot.module.edu.controller.admin.schedule.vo.TeacherScheduleSaveReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.ScheduleExceptionDO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.TeacherScheduleDO;

import java.time.LocalDate;
import java.util.List;

/**
 * 教师排课 Service
 *
 * <p>包含两张表的编排:teacher_schedule(每周时段)+ schedule_exception(单日例外)。
 * <p>注意:本 Service 只做 CRUD 与基本校验;
 *    "学生在某日某时区下可约时段计算"是 booking 子域核心算法,在那里实现。
 */
public interface ScheduleService {

    // ========== teacher_schedule ==========

    List<TeacherScheduleDO> getSchedulesByTeacher(Long teacherId);

    Long createSchedule(TeacherScheduleSaveReqVO reqVO);

    void updateSchedule(TeacherScheduleSaveReqVO reqVO);

    void deleteSchedule(Long id);

    // ========== schedule_exception ==========

    /**
     * 按教师 + 日期范围查例外记录;from / to 可空(NULL 时不限)
     */
    List<ScheduleExceptionDO> getExceptions(Long teacherId, LocalDate from, LocalDate to);

    Long createException(ScheduleExceptionSaveReqVO reqVO);

    void deleteException(Long id);

    // ========== M6 §3.7 A6:教师 app 周模板 / 例外切换 ==========

    /**
     * 教师 app 端切换周模板单格子(M6 §3.7 / §5.1 toggle)。
     *
     * <p>语义:
     * <ul>
     *   <li>available=true → 在 (weekday, hh:mm) 新增 30-min 区间(已存在精确匹配的同起点区间则幂等返回 null)</li>
     *   <li>available=false → 删除 (weekday, hh:mm) 起点的 30-min 区间;
     *       覆盖该格子的更大区间不在本端处理(返回 null,前端走详细编辑)</li>
     * </ul>
     *
     * @return 新增成功返回新 id;删除或幂等返回 null
     */
    Long toggleWeeklySlot(Long teacherId, Integer weekday, Integer hh, Integer mm,
                          Boolean available, String timezone);

    /**
     * 教师 app 端单次例外操作(M6 §3.7 / §5.1 exceptions POST)。
     *
     * <p>action:
     * <ul>
     *   <li>closed/extra → 新增对应 type 的 30-min 例外(已存在同 (date, hh:mm, type) 则幂等)</li>
     *   <li>remove → 删除 (date, hh:mm) 起点的所有例外(closed + extra)</li>
     * </ul>
     *
     * @return 新增成功返回新 id;remove/幂等返回 null
     */
    Long toggleException(Long teacherId, java.time.LocalDate date, Integer hh, Integer mm,
                         String action, String timezone, String reason);

}
