package com.mandarly.boot.module.edu.service.balance;

import com.mandarly.boot.module.edu.dal.dataobject.income.TeacherBalanceDO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 教师余额服务接口
 *
 * <p>不变量(对齐 db-design v1.2 §4.3):
 * available + frozen_t7 + pending_withdraw = total_earned - total_withdrawn
 *
 * <p>所有写方法走 `@Transactional` + 乐观锁(version),并发冲突抛 `ServiceException(BALANCE_VERSION_CONFLICT)`。
 * 调用方负责持有 Redisson `teacher:lock:{teacherId}` 锁(spec §3.3 统一 key)。
 */
public interface TeacherBalanceService {

    // ======================== 原 M4 rebuild 接口(A5 改造,A3 不动)========================

    /**
     * 重建指定 teachers 的 balance(Job 全量 / 增量都用此入口)
     *
     * @param teacherIds 教师 ID 列表
     * @return 更新条数
     */
    int rebuildBalances(List<Long> teacherIds);

    /**
     * 全量重建(daily Job)
     *
     * @return 更新条数
     */
    int rebuildAll();

    /**
     * 查单个教师当前 balance
     *
     * @param teacherId 教师 ID
     * @return TeacherBalanceDO(可空)
     */
    TeacherBalanceDO getBalance(Long teacherId);

    // ======================== M6 新增方法(spec §3.6)========================

    /**
     * 入账(T+7 冻结):frozen_t7 += amount,total_earned += amount。
     * 课程结算 settle 时,与 teacher_income 写入同事务调用。
     *
     * @param teacherId 教师 id
     * @param amount    金额(USD,>0)
     * @param currency  币种(目前固定 USD,新建 balance 时写入)
     */
    void freeze(Long teacherId, BigDecimal amount, String currency);

    /**
     * 解冻(T+7 到期):frozen_t7 -= amount,available += amount。
     * UnfreezeIncomeJob(A5)按 teacher 分组扫到期 income 时调用。
     */
    void unfreeze(Long teacherId, BigDecimal amount);

    /**
     * 提现申请:available -= amount,pending_withdraw += amount。
     */
    void holdForWithdrawal(Long teacherId, BigDecimal amount);

    /**
     * 提现驳回 / 打款失败:pending_withdraw -= amount,available += amount。
     */
    void releaseFromWithdrawal(Long teacherId, BigDecimal amount);

    /**
     * 提现打款成功:pending_withdraw -= amount,total_withdrawn += amount。
     */
    void confirmWithdrawal(Long teacherId, BigDecimal amount);

    /**
     * 退款扣回 — 原 income 已解冻(status=available):
     * available -= amount,total_earned -= amount。
     *
     * <p>若 available 不够(教师已提走),允许负数 + 告警(spec §3.4 / db-design §1.5.2 case 3)。
     */
    void deductFromAvailable(Long teacherId, BigDecimal amount);

    /**
     * 退款扣回 — 原 income 仍在冻结期(status=frozen):
     * frozen_t7 -= amount,total_earned -= amount。
     *
     * <p>同样允许负数(理论上不会发生,防御性)。
     */
    void deductFromFrozen(Long teacherId, BigDecimal amount);
}
