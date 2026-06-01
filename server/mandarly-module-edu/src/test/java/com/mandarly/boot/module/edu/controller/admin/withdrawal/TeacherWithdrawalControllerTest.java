package com.mandarly.boot.module.edu.controller.admin.withdrawal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.framework.security.core.util.SecurityFrameworkUtils;
import com.mandarly.boot.framework.web.core.handler.GlobalExceptionHandler;
import com.mandarly.boot.module.edu.dal.dataobject.withdrawal.TeacherWithdrawalDO;
import com.mandarly.boot.module.edu.service.withdrawal.TeacherWithdrawalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TeacherWithdrawalController @MockMvc smoke 测试(M6 A7)
 *
 * <p>覆盖 5 case:
 * <ul>
 *   <li>{@code page_returns_200_with_masked_payee} — 列表 200 + payee 脱敏</li>
 *   <li>{@code audit_approved_returns_200} — 通过审核 happy path</li>
 *   <li>{@code audit_missing_approved_returns_400} — VO 层 approved 必填校验</li>
 *   <li>{@code audit_rejected_without_reason_returns_service_error} — Service 内置 reject 必填校验</li>
 *   <li>{@code mark_failed_without_reason_returns_400} — VO 层 @NotBlank 校验</li>
 *   <li>{@code reveal_payee_returns_unmasked} — 走 reveal-payee 走 Service.getUnmaskedPayeeInfo</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class TeacherWithdrawalControllerTest {

    @Mock
    private TeacherWithdrawalService teacherWithdrawalService;

    @InjectMocks
    private TeacherWithdrawalController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MockedStatic<SecurityFrameworkUtils> securityStatic;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void teardown() {
        if (securityStatic != null) {
            securityStatic.close();
        }
    }

    private TeacherWithdrawalDO sampleRow(Long id, String status) {
        TeacherWithdrawalDO row = new TeacherWithdrawalDO();
        row.setId(id);
        row.setTeacherId(2048L);
        row.setAmount(new BigDecimal("200.00"));
        row.setCurrency("USD");
        row.setPayeeInfo("alipay:13900001111:张三");
        row.setPayeeMethod("alipay");
        row.setStatus(status);
        row.setAppliedAt(LocalDateTime.parse("2026-05-15T10:00:00"));
        return row;
    }

    private MockMvc mockMvcWithExceptionAdvice() {
        return MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler("mandarly-test", null))
                .build();
    }

    @Test
    void page_returns_200_with_masked_payee() throws Exception {
        TeacherWithdrawalDO row = sampleRow(30001L, "pending");
        when(teacherWithdrawalService.getPageForAdmin(any()))
                .thenReturn(new PageResult<>(List.of(row), 1L));

        mockMvc.perform(get("/edu/withdrawal/page").param("status", "pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(30001))
                .andExpect(jsonPath("$.data.list[0].status").value("pending"))
                // payee_info 必须脱敏 — codepoint 长度 21,last 4 = "1:张三"
                .andExpect(jsonPath("$.data.list[0].payeeInfoMasked").value("alipay·****1:张三"))
                // 明文必须不返回
                .andExpect(jsonPath("$.data.list[0].payeeInfo").doesNotExist());
    }

    @Test
    void audit_approved_returns_200() throws Exception {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(999L);

        String body = "{\"approved\":true}";
        mockMvc.perform(post("/edu/withdrawal/30001/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        verify(teacherWithdrawalService).audit(eq(30001L), eq(true), eq(null), eq(999L));
    }

    @Test
    void audit_missing_approved_returns_400() throws Exception {
        String body = "{\"rejectReason\":\"\"}"; // approved 缺失,VO 层 @NotNull 拒
        mockMvc.perform(post("/edu/withdrawal/30001/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(teacherWithdrawalService);
    }

    @Test
    void audit_rejected_without_reason_returns_service_error() throws Exception {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(999L);

        doThrow(new ServiceException(1_004_504, "驳回必须填写原因"))
                .when(teacherWithdrawalService).audit(eq(30001L), eq(false), eq(""), eq(999L));

        String body = "{\"approved\":false,\"rejectReason\":\"\"}";
        mockMvcWithExceptionAdvice().perform(post("/edu/withdrawal/30001/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1_004_504))
                .andExpect(jsonPath("$.msg").value("驳回必须填写原因"));

        verify(teacherWithdrawalService).audit(eq(30001L), eq(false), eq(""), eq(999L));
    }

    @Test
    void mark_failed_without_reason_returns_400() throws Exception {
        // failReason 空 → VO 层 @NotBlank 拒绝
        String body = "{\"failReason\":\"\"}";
        mockMvc.perform(post("/edu/withdrawal/30001/mark-failed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(teacherWithdrawalService);
    }

    @Test
    void reveal_payee_returns_unmasked() throws Exception {
        securityStatic = Mockito.mockStatic(SecurityFrameworkUtils.class);
        securityStatic.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(999L);

        when(teacherWithdrawalService.getUnmaskedPayeeInfo(eq(30001L), eq(999L)))
                .thenReturn("alipay:13900001111:张三");

        mockMvc.perform(get("/edu/withdrawal/30001/reveal-payee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("alipay:13900001111:张三"));

        verify(teacherWithdrawalService).getUnmaskedPayeeInfo(eq(30001L), eq(999L));
        // 二次安全验证:本接口不应触发任何 mask 路径 / 列表查询
        verify(teacherWithdrawalService, never()).getPageForAdmin(any());
        verify(teacherWithdrawalService, never()).audit(anyLong(), org.mockito.ArgumentMatchers.anyBoolean(), anyString(), anyLong());
    }
}
