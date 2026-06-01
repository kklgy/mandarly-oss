package com.mandarly.boot.module.edu.service.withdrawal;

import com.mandarly.boot.framework.common.pojo.PageParam;
import com.mandarly.boot.framework.common.pojo.PageResult;
import com.mandarly.boot.module.edu.controller.admin.withdrawal.vo.WithdrawalPageReqVO;
import com.mandarly.boot.module.edu.dal.dataobject.withdrawal.TeacherWithdrawalDO;

import java.math.BigDecimal;

/**
 * 教师提现 Service(M6 §3.1 / §3.2)
 *
 * <p>状态机:pending → approved → (paid | failed);pending → rejected。
 *
 * <p>并发保护(§3.3):所有 余额相关写操作 持 Redisson 锁 {@code teacher:lock:{teacherId}},
 * 与 settle / unfreeze / deductForRefund 互斥。
 *
 * <p>余额联动:
 * <ul>
 *   <li>applyWithdrawal → balance.holdForWithdrawal(available → pending_withdraw)</li>
 *   <li>audit(rejected) → balance.releaseFromWithdrawal(pending_withdraw → available)</li>
 *   <li>markPaid → balance.confirmWithdrawal(pending_withdraw → total_withdrawn)</li>
 *   <li>markFailed → balance.releaseFromWithdrawal(pending_withdraw → available)</li>
 *   <li>audit(approved) → 不动余额</li>
 * </ul>
 */
public interface TeacherWithdrawalService {

    /**
     * 教师申请提现。
     *
     * @param teacherId   教师 user.id
     * @param amount      申请金额(USD,> 0)
     * @param payeeInfo   收款信息明文(JSON 或单串),Service 透传,落库由 TypeHandler 加密
     * @param payeeMethod 收款方式(对应 {@link com.mandarly.boot.module.edu.enums.withdrawal.PayeeMethodEnum})
     * @return 新申请 id
     */
    Long applyWithdrawal(Long teacherId, BigDecimal amount, String payeeInfo, String payeeMethod);

    /**
     * Admin 审核(pending → approved / rejected)。
     *
     * <p>approved:不动余额,仅写 auditedBy / auditedAt。
     * <p>rejected:require rejectReason;余额 pending_withdraw → available。
     */
    void audit(Long id, boolean approved, String rejectReason, Long auditorId);

    /**
     * Admin 标记打款成功(approved → paid)。pending_withdraw -= amount / total_withdrawn += amount。
     */
    void markPaid(Long id, String paidProof, String paidRemark, Long operatorId);

    /**
     * Admin 标记打款失败(approved → failed)。pending_withdraw → available;require failReason。
     */
    void markFailed(Long id, String failReason, Long operatorId);

    /**
     * 教师查自己的提现历史(分页)。
     */
    PageResult<TeacherWithdrawalDO> getPageForTeacher(Long teacherId, String status, PageParam pageParam);

    /**
     * Admin 查所有提现(分页,多过滤)。
     */
    PageResult<TeacherWithdrawalDO> getPageForAdmin(WithdrawalPageReqVO req);

    /**
     * 取详情(payee_info 已由 TypeHandler 解密为明文)。
     * <p>Controller 层负责脱敏展示(教师视角 / admin 列表视角)。
     */
    TeacherWithdrawalDO getDetail(Long id);

    /**
     * Admin 查看完整 payee_info(写入审计日志)。
     *
     * <p><strong>安全约束</strong>:Service 信任调用方,不做权限 / status 校验。
     * 唯一 gate 是 A7 admin Controller 的 {@code @PreAuthorize("@ss.hasPermission('edu:withdrawal:reveal-payee')")} +
     * 二次确认弹层(spec §6.5)。allowed 在任意 status 下查看历史记录(审计 / 客诉用)。
     *
     * @param id      提现申请 id
     * @param adminId 当前管理员 user.id(写入审计日志)
     * @return 完整明文 payee_info
     */
    String getUnmaskedPayeeInfo(Long id, Long adminId);
}
