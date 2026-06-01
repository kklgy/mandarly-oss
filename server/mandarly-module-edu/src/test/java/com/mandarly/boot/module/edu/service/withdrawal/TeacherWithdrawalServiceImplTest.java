package com.mandarly.boot.module.edu.service.withdrawal;

import com.mandarly.boot.framework.common.exception.ServiceException;
import com.mandarly.boot.framework.test.core.ut.BaseMockitoUnitTest;
import com.mandarly.boot.module.edu.dal.dataobject.withdrawal.TeacherWithdrawalDO;
import com.mandarly.boot.module.edu.dal.mysql.withdrawal.TeacherWithdrawalMapper;
import com.mandarly.boot.module.edu.service.balance.TeacherBalanceService;
import com.mandarly.boot.module.edu.service.notification.NotificationService;
import com.mandarly.boot.module.infra.api.config.ConfigApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TeacherWithdrawalServiceImpl TDD 单元测试(spec §8.2 case 1-10)
 */
class TeacherWithdrawalServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private TeacherWithdrawalServiceImpl teacherWithdrawalService;

    @Mock
    private TeacherWithdrawalMapper teacherWithdrawalMapper;

    @Mock
    private TeacherBalanceService teacherBalanceService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private ConfigApi configApi;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setupMinAmount() {
        // A7 起 min-amount 走 ConfigApi.getConfigValueByKey,默认值由 Service 内 fallback 100。
        // 用 lenient stub 让大部分 case 取到 "100",case 4 验证 below_min 时直接进入预检拒绝路径。
        lenient().when(configApi.getConfigValueByKey("mandarly.withdrawal.min_amount"))
                .thenReturn("100");
    }

    // ======================== helpers ========================

    private TeacherWithdrawalDO withdrawal(Long id, Long teacherId, BigDecimal amount, String status) {
        TeacherWithdrawalDO w = new TeacherWithdrawalDO();
        w.setId(id);
        w.setTeacherId(teacherId);
        w.setAmount(amount);
        w.setCurrency("USD");
        w.setStatus(status);
        w.setPayeeInfo("alipay:13900001111:张三");
        w.setPayeeMethod("alipay");
        return w;
    }

    /** stub Redisson lock — tryLock 永远成功 */
    private RLock stubLock() {
        RLock lock = mock(RLock.class);
        try {
            lenient().when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        lenient().when(lock.isHeldByCurrentThread()).thenReturn(true);
        return lock;
    }

    /** stub TransactionTemplate.executeWithoutResult — 直接执行回调 */
    private void stubTxTemplate() {
        lenient().doAnswer(inv -> {
            java.util.function.Consumer<Object> callback = inv.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    // ======================== case 1-4: applyWithdrawal ========================

    /** case 1: 余额够 → withdrawal pending + balance.holdForWithdrawal 被调 */
    @Test
    void apply_balance_enough_pending_created() {
        Long teacherId = 10L;
        BigDecimal amount = new BigDecimal("200.00");

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:10"))).thenReturn(lock);
        when(teacherWithdrawalMapper.selectByTeacherIdAndStatusIn(eq(teacherId), any()))
                .thenReturn(Collections.emptyList());
        stubTxTemplate();
        // mock insert → 写 id
        doAnswer(inv -> {
            TeacherWithdrawalDO arg = inv.getArgument(0);
            arg.setId(8001L);
            return 1;
        }).when(teacherWithdrawalMapper).insert(any(TeacherWithdrawalDO.class));

        Long id = teacherWithdrawalService.applyWithdrawal(teacherId, amount, "alipay:139:Z", "alipay");

        assertThat(id).isEqualTo(8001L);
        verify(teacherBalanceService).holdForWithdrawal(eq(teacherId), eq(amount));
        verify(teacherWithdrawalMapper).insert(argThat((TeacherWithdrawalDO w) ->
                w.getTeacherId().equals(teacherId)
                        && w.getAmount().compareTo(amount) == 0
                        && "pending".equals(w.getStatus())
                        && "alipay".equals(w.getPayeeMethod())
                        && "alipay:139:Z".equals(w.getPayeeInfo())
                        && w.getAppliedAt() != null
        ));
    }

    /**
     * case 2: 余额不够 → balance.holdForWithdrawal 内部抛 BALANCE_VERSION_CONFLICT(乐观锁失败)。
     * Service 应透传该异常,不写 withdrawal 行。
     */
    @Test
    void apply_balance_short_throws_optimistic_lock_conflict() {
        Long teacherId = 11L;
        BigDecimal amount = new BigDecimal("200.00");

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:11"))).thenReturn(lock);
        when(teacherWithdrawalMapper.selectByTeacherIdAndStatusIn(eq(teacherId), any()))
                .thenReturn(Collections.emptyList());
        stubTxTemplate();
        // balance.holdForWithdrawal 抛 ServiceException(模拟乐观锁失败 / 余额不足)
        doThrow(new ServiceException(1_004_302, "余额并发冲突,请重试"))
                .when(teacherBalanceService).holdForWithdrawal(eq(teacherId), eq(amount));

        assertThatThrownBy(() ->
                teacherWithdrawalService.applyWithdrawal(teacherId, amount, "alipay:139:Z", "alipay")
        ).isInstanceOf(ServiceException.class);

        verify(teacherWithdrawalMapper, never()).insert(any(TeacherWithdrawalDO.class));
    }

    /** case 3: 已有 pending → throw WITHDRAWAL_PENDING_EXISTS,不调 balance / 不写 */
    @Test
    void apply_duplicate_pending_throws_WITHDRAWAL_PENDING_EXISTS() {
        Long teacherId = 12L;
        BigDecimal amount = new BigDecimal("150.00");

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:12"))).thenReturn(lock);
        TeacherWithdrawalDO existing = withdrawal(7777L, teacherId, new BigDecimal("100"), "pending");
        when(teacherWithdrawalMapper.selectByTeacherIdAndStatusIn(eq(teacherId), any()))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() ->
                teacherWithdrawalService.applyWithdrawal(teacherId, amount, "alipay:139:Z", "alipay")
        ).isInstanceOf(ServiceException.class)
         .hasMessageContaining("已有进行中的提现申请");

        verify(teacherBalanceService, never()).holdForWithdrawal(any(), any());
        verify(teacherWithdrawalMapper, never()).insert(any(TeacherWithdrawalDO.class));
    }

    /**
     * case 4: amount < min → throw WITHDRAWAL_BELOW_MIN。
     * Service 在锁外做轻量预检 → 此时不应获取锁 / 不应查重 / 不应动余额。
     */
    @Test
    void apply_below_min_throws_WITHDRAWAL_BELOW_MIN() {
        Long teacherId = 13L;
        BigDecimal amount = new BigDecimal("50.00"); // < min(100)

        assertThatThrownBy(() ->
                teacherWithdrawalService.applyWithdrawal(teacherId, amount, "alipay:139:Z", "alipay")
        ).isInstanceOf(ServiceException.class)
         .hasMessageContaining("低于最低额度");

        // 锁外早抛 → 不应锁也不应查重
        verify(redissonClient, never()).getLock(any(String.class));
        verify(teacherWithdrawalMapper, never()).selectByTeacherIdAndStatusIn(any(), any());
        verify(teacherBalanceService, never()).holdForWithdrawal(any(), any());
        verify(teacherWithdrawalMapper, never()).insert(any(TeacherWithdrawalDO.class));
    }

    // ======================== case 5-6: audit ========================

    /** case 5: audit approved → 状态变 approved,不动余额 */
    @Test
    void audit_approved_no_balance_change() {
        Long id = 8001L;
        Long teacherId = 20L;
        BigDecimal amount = new BigDecimal("200.00");
        Long auditorId = 999L;

        TeacherWithdrawalDO existing = withdrawal(id, teacherId, amount, "pending");
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:20"))).thenReturn(lock);
        stubTxTemplate();

        teacherWithdrawalService.audit(id, true, null, auditorId);

        verify(teacherWithdrawalMapper).updateById(argThat((TeacherWithdrawalDO upd) ->
                upd.getId().equals(id)
                        && "approved".equals(upd.getStatus())
                        && auditorId.equals(upd.getAuditedBy())
                        && upd.getAuditedAt() != null
        ));
        // approved 不动余额
        verify(teacherBalanceService, never()).releaseFromWithdrawal(any(), any());
        verify(teacherBalanceService, never()).confirmWithdrawal(any(), any());
    }

    /** case 6: audit rejected → balance.releaseFromWithdrawal + reject_reason required */
    @Test
    void audit_rejected_balance_restored() {
        Long id = 8002L;
        Long teacherId = 21L;
        BigDecimal amount = new BigDecimal("300.00");
        Long auditorId = 999L;

        TeacherWithdrawalDO existing = withdrawal(id, teacherId, amount, "pending");
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:21"))).thenReturn(lock);
        stubTxTemplate();

        // 6a: rejectReason 空 → throw
        assertThatThrownBy(() ->
                teacherWithdrawalService.audit(id, false, "  ", auditorId)
        ).isInstanceOf(ServiceException.class)
         .hasMessageContaining("驳回必须填写原因");

        verify(teacherBalanceService, never()).releaseFromWithdrawal(any(), any());

        // 6b: rejectReason 有 → 正常走
        teacherWithdrawalService.audit(id, false, "材料不全", auditorId);

        verify(teacherBalanceService).releaseFromWithdrawal(eq(teacherId), eq(amount));
        verify(teacherWithdrawalMapper).updateById(argThat((TeacherWithdrawalDO upd) ->
                upd.getId().equals(id)
                        && "rejected".equals(upd.getStatus())
                        && "材料不全".equals(upd.getRejectReason())
                        && auditorId.equals(upd.getAuditedBy())
                        && upd.getAuditedAt() != null
        ));
    }

    // ======================== case 7-8: markPaid / markFailed ========================

    /** case 7: markPaid (approved → paid) → balance.confirmWithdrawal */
    @Test
    void markPaid_balance_settled() {
        Long id = 8003L;
        Long teacherId = 22L;
        BigDecimal amount = new BigDecimal("250.00");
        Long operatorId = 999L;

        TeacherWithdrawalDO existing = withdrawal(id, teacherId, amount, "approved");
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:22"))).thenReturn(lock);
        stubTxTemplate();

        teacherWithdrawalService.markPaid(id, "https://cos/proof.png", "已转账", operatorId);

        verify(teacherBalanceService).confirmWithdrawal(eq(teacherId), eq(amount));
        verify(teacherWithdrawalMapper).updateById(argThat((TeacherWithdrawalDO upd) ->
                upd.getId().equals(id)
                        && "paid".equals(upd.getStatus())
                        && operatorId.equals(upd.getPaidBy())
                        && "https://cos/proof.png".equals(upd.getPaidProof())
                        && "已转账".equals(upd.getPaidRemark())
                        && upd.getPaidAt() != null
        ));
        verify(teacherBalanceService, never()).releaseFromWithdrawal(any(), any());
    }

    /** case 8: markFailed (approved → failed) → balance.releaseFromWithdrawal */
    @Test
    void markFailed_balance_restored() {
        Long id = 8004L;
        Long teacherId = 23L;
        BigDecimal amount = new BigDecimal("180.00");
        Long operatorId = 999L;

        TeacherWithdrawalDO existing = withdrawal(id, teacherId, amount, "approved");
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:23"))).thenReturn(lock);
        stubTxTemplate();

        // 8a: failReason 空 → throw
        assertThatThrownBy(() ->
                teacherWithdrawalService.markFailed(id, "  ", operatorId)
        ).isInstanceOf(ServiceException.class)
         .hasMessageContaining("打款失败必须填写原因");
        verify(teacherBalanceService, never()).releaseFromWithdrawal(any(), any());

        // 8b: 正常
        teacherWithdrawalService.markFailed(id, "账号错误", operatorId);

        verify(teacherBalanceService).releaseFromWithdrawal(eq(teacherId), eq(amount));
        verify(teacherWithdrawalMapper).updateById(argThat((TeacherWithdrawalDO upd) ->
                upd.getId().equals(id)
                        && "failed".equals(upd.getStatus())
                        && "账号错误".equals(upd.getPaidRemark())
                        && operatorId.equals(upd.getPaidBy())
                        && upd.getPaidAt() != null
        ));
        verify(teacherBalanceService, never()).confirmWithdrawal(any(), any());
    }

    // ======================== case 9: 非法跃迁 ========================

    /** case 9: pending → paid 非法跃迁 → throw WITHDRAWAL_INVALID_STATUS */
    @Test
    void invalid_transition_pending_to_paid_throws_WITHDRAWAL_INVALID_STATUS() {
        Long id = 8005L;
        Long teacherId = 24L;
        BigDecimal amount = new BigDecimal("100.00");

        // 状态 pending,试图 markPaid(只允许 approved → paid)
        TeacherWithdrawalDO existing = withdrawal(id, teacherId, amount, "pending");
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        assertThatThrownBy(() ->
                teacherWithdrawalService.markPaid(id, "url", "remark", 999L)
        ).isInstanceOf(ServiceException.class)
         .hasMessageContaining("提现状态不允许此操作");

        verify(teacherBalanceService, never()).confirmWithdrawal(any(), any());
        verify(teacherWithdrawalMapper, never()).updateById(any(TeacherWithdrawalDO.class));
    }

    // ======================== case 10: revealPayee ========================

    /** case 10: getUnmaskedPayeeInfo → AesEncryptTypeHandler 透明解密后返回明文。
     *
     *  审计日志走 {@code @LogRecord} AOP(spec §6.5),由 mzt-biz-log 拦截写到 system_operate_log 表;
     *  AOP 在 Spring context 之外不会触发,单测只验证业务返回值;审计落库由 E2 e2e + Phase E staging 真发覆盖。
     */
    @Test
    void revealPayee_decrypts() {
        Long id = 8006L;
        Long teacherId = 25L;
        Long adminId = 31337L;

        TeacherWithdrawalDO existing = withdrawal(id, teacherId, new BigDecimal("200"), "pending");
        existing.setPayeeInfo("alipay:13900001111:张三"); // 这就是 TypeHandler 解密后的明文
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        String result = teacherWithdrawalService.getUnmaskedPayeeInfo(id, adminId);

        assertThat(result).isEqualTo("alipay:13900001111:张三");
        verify(teacherWithdrawalMapper).selectById(id);
    }

    // ======================== case 11-15: NotificationService hook(D3) ========================

    /** case 11: applyWithdrawal 成功 → 主事务后 sendForWithdrawalEvent("applied", row) 被调 */
    @Test
    void apply_success_triggers_notification_applied() {
        Long teacherId = 30L;
        BigDecimal amount = new BigDecimal("200.00");

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:30"))).thenReturn(lock);
        when(teacherWithdrawalMapper.selectByTeacherIdAndStatusIn(eq(teacherId), any()))
                .thenReturn(Collections.emptyList());
        stubTxTemplate();
        doAnswer(inv -> {
            TeacherWithdrawalDO arg = inv.getArgument(0);
            arg.setId(9001L);
            return 1;
        }).when(teacherWithdrawalMapper).insert(any(TeacherWithdrawalDO.class));
        // hook 内部再次 selectById 取最新 row
        TeacherWithdrawalDO committed = withdrawal(9001L, teacherId, amount, "pending");
        when(teacherWithdrawalMapper.selectById(9001L)).thenReturn(committed);

        teacherWithdrawalService.applyWithdrawal(teacherId, amount, "alipay:139:Z", "alipay");

        verify(notificationService).sendForWithdrawalEvent(eq("applied"), argThat(w ->
                w != null && 9001L == w.getId() && "pending".equals(w.getStatus())
        ));
    }

    /** case 12: audit(approved=true) → sendForWithdrawalEvent("approved", row) 被调 */
    @Test
    void audit_approved_triggers_notification_approved() {
        Long id = 9002L;
        Long teacherId = 31L;
        BigDecimal amount = new BigDecimal("250.00");

        TeacherWithdrawalDO existing = withdrawal(id, teacherId, amount, "pending");
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:31"))).thenReturn(lock);
        stubTxTemplate();

        teacherWithdrawalService.audit(id, true, null, 999L);

        verify(notificationService).sendForWithdrawalEvent(eq("approved"), argThat(w ->
                w != null && id.equals(w.getId())
        ));
        verify(notificationService, never()).sendForWithdrawalEvent(eq("rejected"), any());
    }

    /** case 13: audit(approved=false) + 有 reason → sendForWithdrawalEvent("rejected", row) 被调 */
    @Test
    void audit_rejected_triggers_notification_rejected() {
        Long id = 9003L;
        Long teacherId = 32L;
        BigDecimal amount = new BigDecimal("180.00");

        TeacherWithdrawalDO existing = withdrawal(id, teacherId, amount, "pending");
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:32"))).thenReturn(lock);
        stubTxTemplate();

        teacherWithdrawalService.audit(id, false, "材料不全", 999L);

        verify(notificationService).sendForWithdrawalEvent(eq("rejected"), argThat(w ->
                w != null && id.equals(w.getId())
        ));
        verify(notificationService, never()).sendForWithdrawalEvent(eq("approved"), any());
    }

    /** case 14: markPaid → sendForWithdrawalEvent("paid", row) 被调 */
    @Test
    void markPaid_triggers_notification_paid() {
        Long id = 9004L;
        Long teacherId = 33L;
        BigDecimal amount = new BigDecimal("300.00");

        TeacherWithdrawalDO existing = withdrawal(id, teacherId, amount, "approved");
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:33"))).thenReturn(lock);
        stubTxTemplate();

        teacherWithdrawalService.markPaid(id, "https://cos/proof.png", "已转账", 999L);

        verify(notificationService).sendForWithdrawalEvent(eq("paid"), argThat(w ->
                w != null && id.equals(w.getId())
        ));
    }

    /** case 15: markFailed → sendForWithdrawalEvent("failed", row) 被调 */
    @Test
    void markFailed_triggers_notification_failed() {
        Long id = 9005L;
        Long teacherId = 34L;
        BigDecimal amount = new BigDecimal("150.00");

        TeacherWithdrawalDO existing = withdrawal(id, teacherId, amount, "approved");
        when(teacherWithdrawalMapper.selectById(id)).thenReturn(existing);

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:34"))).thenReturn(lock);
        stubTxTemplate();

        teacherWithdrawalService.markFailed(id, "账号错误", 999L);

        verify(notificationService).sendForWithdrawalEvent(eq("failed"), argThat(w ->
                w != null && id.equals(w.getId())
        ));
    }

    /** case 16: 业务前置失败(余额不足)→ 不应触发通知 */
    @Test
    void apply_balance_short_no_notification() {
        Long teacherId = 35L;
        BigDecimal amount = new BigDecimal("200.00");

        RLock lock = stubLock();
        when(redissonClient.getLock(eq("teacher:lock:35"))).thenReturn(lock);
        when(teacherWithdrawalMapper.selectByTeacherIdAndStatusIn(eq(teacherId), any()))
                .thenReturn(Collections.emptyList());
        stubTxTemplate();
        doThrow(new ServiceException(1_004_302, "余额并发冲突,请重试"))
                .when(teacherBalanceService).holdForWithdrawal(eq(teacherId), eq(amount));

        assertThatThrownBy(() ->
                teacherWithdrawalService.applyWithdrawal(teacherId, amount, "alipay:139:Z", "alipay")
        ).isInstanceOf(ServiceException.class);

        // 主事务回滚 / 业务前置失败 → 通知不应触发
        verify(notificationService, never()).sendForWithdrawalEvent(anyString(), any());
    }
}
