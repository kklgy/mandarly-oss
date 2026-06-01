package com.mandarly.boot.module.edu.controller.app.schedule;

import com.mandarly.boot.module.edu.dal.dataobject.schedule.ScheduleExceptionDO;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.TeacherScheduleDO;
import com.mandarly.boot.module.edu.service.schedule.ScheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppTeacherScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private AppTeacherScheduleController controller;

    @Test
    void listSchedules_returnsRowsForAnonymousVisitors() {
        TeacherScheduleDO row = new TeacherScheduleDO();
        row.setId(10001L);
        row.setTeacherId(24L);
        row.setWeekday(1);
        row.setStartTime(LocalTime.of(19, 0));
        row.setEndTime(LocalTime.of(19, 30));
        row.setTimezone("Asia/Hong_Kong");
        when(scheduleService.getSchedulesByTeacher(24L)).thenReturn(List.of(row));

        var data = controller.listSchedules(24L).getData();

        assertEquals(1, data.size());
        assertEquals(24L, data.get(0).getTeacherId());
        assertEquals(1, data.get(0).getWeekday());
        assertEquals(LocalTime.of(19, 0), data.get(0).getStartTime());
    }

    @Test
    void listExceptions_returnsRowsForAnonymousVisitors() {
        ScheduleExceptionDO row = new ScheduleExceptionDO();
        row.setId(20001L);
        row.setTeacherId(24L);
        row.setExceptionDate(LocalDate.of(2026, 5, 25));
        row.setType("extra");
        row.setStartTime(LocalTime.of(20, 0));
        row.setEndTime(LocalTime.of(20, 30));
        row.setTimezone("Asia/Hong_Kong");
        when(scheduleService.getExceptions(24L, LocalDate.of(2026, 5, 25), LocalDate.of(2026, 5, 31)))
                .thenReturn(List.of(row));

        var data = controller.listExceptions(
                24L,
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31)).getData();

        assertEquals(1, data.size());
        assertEquals(24L, data.get(0).getTeacherId());
        assertEquals("extra", data.get(0).getType());
        assertEquals(LocalTime.of(20, 0), data.get(0).getStartTime());
    }
}
