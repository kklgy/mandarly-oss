package com.mandarly.boot.module.edu.controller.app.teacher_center;

import com.mandarly.boot.module.edu.service.withdrawal.TeacherWithdrawalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AppTeacherWithdrawalController @MockMvc smoke 测试(M6 A6 Step 2)
 *
 * <p>覆盖 validation 失败路径:POST /apply 金额 0 触发 @DecimalMin 校验 → HTTP 400 + Service 未被调用。
 */
@ExtendWith(MockitoExtension.class)
class AppTeacherWithdrawalControllerTest {

    @Mock
    private TeacherWithdrawalService teacherWithdrawalService;

    @InjectMocks
    private AppTeacherWithdrawalController controller;

    @Test
    void apply_invalid_amount_returns_400() throws Exception {
        // amount = 0 触发 @DecimalMin("0.01");payeeInfo 空触发 @NotBlank。
        // 任一字段校验失败即可拒绝;Service 必须未被调用。
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        String invalidBody = "{\"amount\":0,\"payeeInfo\":\"\",\"payeeMethod\":\"paypal\"}";
        mockMvc.perform(post("/edu/teacher-center/withdrawal/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(teacherWithdrawalService);
    }
}
