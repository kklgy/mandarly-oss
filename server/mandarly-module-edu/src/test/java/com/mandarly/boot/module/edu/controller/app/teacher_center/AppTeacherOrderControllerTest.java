package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.dal.dataobject.booking.CourseOrderDO;
import com.mandarly.boot.module.edu.dal.dataobject.user.UserDO;
import com.mandarly.boot.module.edu.dal.mysql.user.EduUserMapper;
import com.mandarly.boot.module.edu.service.booking.BookingService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AppTeacherOrderController @MockMvc smoke 测试(M6 A6 Step 2)
 *
 * <p>覆盖 happy path:GET /list?status=upcoming 返回 200 + 含订单列表 + 学生 nickname。
 */
@ExtendWith(MockitoExtension.class)
class AppTeacherOrderControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private EduUserMapper eduUserMapper;

    @InjectMocks
    private AppTeacherOrderController controller;

    private MockMvc mockMvc;
    private MockedStatic<SecurityFrameworkUtils> securityStatic;

    @AfterEach
    void teardown() {
        if (securityStatic != null) {
            securityStatic.close();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void list_returns_200_with_upcoming_status() throws Exception {
        // given
        Long teacherId = 2048L;
        Long studentId = 1024L;
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(teacherId);

        CourseOrderDO order = new CourseOrderDO();
        order.setId(50001L);
        order.setStudentId(studentId);
        order.setTeacherId(teacherId);
        order.setScheduledAt(LocalDateTime.parse("2026-05-20T09:00:00"));
        order.setDuration(30);
        order.setStatus("upcoming");
        order.setTeacherAmount(new BigDecimal("5.00"));
        order.setTeacherSettleStatus("frozen");
        order.setIsFreeTrial(false);

        when(bookingService.getTeacherOrdersPage(eq(teacherId), any(), any()))
                .thenReturn(new PageResult<>(List.of(order), 1L));

        UserDO student = new UserDO();
        student.setId(studentId);
        student.setNickname("Alice");
        student.setAvatarUrl("https://cdn.example/avatar/1024.png");
        when(eduUserMapper.selectById(studentId)).thenReturn(student);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();

        // when / then
        mockMvc.perform(get("/edu/teacher-center/orders/list").param("status", "upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(50001))
                .andExpect(jsonPath("$.data.list[0].studentId").value(studentId))
                .andExpect(jsonPath("$.data.list[0].studentNickname").value("Alice"))
                .andExpect(jsonPath("$.data.list[0].status").value("upcoming"))
                .andExpect(jsonPath("$.data.list[0].teacherSettleStatus").value("frozen"));
    }
}
