package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherBalanceDO;
import com.mandarly.boot.module.edu.dal.mysql.income.TeacherIncomeMapper;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AppTeacherIncomeController @MockMvc smoke 测试(M6 A6 Step 2)
 *
 * <p>覆盖 happy path:GET /balance 返回 200 + 5 个 USD 字段齐全。
 */
@ExtendWith(MockitoExtension.class)
class AppTeacherIncomeControllerTest {

    @Mock
    private TeacherBalanceService teacherBalanceService;

    @Mock
    private TeacherIncomeMapper teacherIncomeMapper;

    @InjectMocks
    private AppTeacherIncomeController controller;

    private MockMvc mockMvc;
    private MockedStatic<SecurityFrameworkUtils> securityStatic;

    @AfterEach
    void teardown() {
        if (securityStatic != null) {
            securityStatic.close();
        }
    }

    @Test
    void balance_returns_200_with_5_fields() throws Exception {
        // given
        Long teacherId = 2048L;
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(teacherId);

        TeacherBalanceDO balance = new TeacherBalanceDO();
        balance.setTeacherId(teacherId);
        balance.setAvailableUsd(new BigDecimal("580.00"));
        balance.setFrozenT7Usd(new BigDecimal("120.00"));
        balance.setPendingWithdrawUsd(new BigDecimal("100.00"));
        balance.setTotalEarnedUsd(new BigDecimal("1200.00"));
        balance.setTotalWithdrawnUsd(new BigDecimal("400.00"));
        balance.setCurrency("USD");
        when(teacherBalanceService.getBalance(teacherId)).thenReturn(balance);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // when / then
        mockMvc.perform(get("/edu/teacher-center/income/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.availableUsd").value(580.00))
                .andExpect(jsonPath("$.data.frozenT7Usd").value(120.00))
                .andExpect(jsonPath("$.data.pendingWithdrawUsd").value(100.00))
                .andExpect(jsonPath("$.data.totalEarnedUsd").value(1200.00))
                .andExpect(jsonPath("$.data.totalWithdrawnUsd").value(400.00))
                .andExpect(jsonPath("$.data.currency").value("USD"));
    }
}
