package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.dal.dataobject.schedule.TeacherScheduleDO;
import com.mandarly.boot.module.edu.service.schedule.ScheduleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalTime;
import java.util.List;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AppTeacherScheduleController @MockMvc smoke 测试(M6 A6 Step 2)
 *
 * <p>覆盖 happy path:GET /weekly 返回 200 + 含 slots 数组 + teacherId 来自登录态。
 * <p>MockMvc 走 standaloneSetup(不拉 Spring 上下文 / 不解析 @PreAuthorize,仅验 HTTP 层逻辑)。
 */
@ExtendWith(MockitoExtension.class)
class AppTeacherScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private AppTeacherScheduleController controller;

    private MockMvc mockMvc;
    private MockedStatic<SecurityFrameworkUtils> securityStatic;

    @AfterEach
    void teardown() {
        if (securityStatic != null) {
            securityStatic.close();
        }
    }

    @Test
    void weekly_returns_200_with_grid_structure() throws Exception {
        // given
        Long teacherId = 2048L;
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(teacherId);

        TeacherScheduleDO row = new TeacherScheduleDO();
        row.setId(10001L);
        row.setTeacherId(teacherId);
        row.setWeekday(1);
        row.setStartTime(LocalTime.of(9, 0));
        row.setEndTime(LocalTime.of(12, 0));
        row.setTimezone("Asia/Hong_Kong");
        when(scheduleService.getSchedulesByTeacher(teacherId)).thenReturn(List.of(row));

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(jacksonConverter())
                .build();

        // when / then
        mockMvc.perform(get("/edu/teacher-center/schedule/weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.teacherId").value(teacherId))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Hong_Kong"))
                .andExpect(jsonPath("$.data.slots[0].weekday").value(1))
                .andExpect(jsonPath("$.data.slots[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$.data.slots[0].endTime").value("12:00:00"));
    }

    /**
     * 对齐 {@code MandarlyJacksonAutoConfiguration}:LocalTime / LocalDate 走 ISO 字符串,
     * 避免默认 [9,0] 数组结构。
     */
    private static MappingJackson2HttpMessageConverter jacksonConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        return new MappingJackson2HttpMessageConverter(mapper);
    }
}
