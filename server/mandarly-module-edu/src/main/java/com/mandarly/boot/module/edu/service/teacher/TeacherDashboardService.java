package com.mandarly.boot.module.edu.service.teacher;

import com.mandarly.boot.module.edu.controller.app.teacher_center.vo.AppTeacherDashboardSummaryRespVO;

/**
 * 教师 - 工作台 Dashboard 聚合 Service(D16 P2 / PRD-v4 §4.2 T0)
 *
 * <p>承接 GET /app-api/edu/teacher-center/dashboard/summary,把 booking + balance + review 一次聚合。
 *
 * <p>时区策略:从 {@code user.timezone} 取,UTC 兜底。
 * <p>窗口对齐:
 * <ul>
 *   <li>本周 — 教师时区周一 00:00 ~ 下周一 00:00(与前端 dayjs().startOf('week').add(1,'day') 一致)</li>
 *   <li>本月 — 教师时区当月 1 号 00:00 ~ 下月 1 号 00:00</li>
 * </ul>
 */
public interface TeacherDashboardService {

    /**
     * 聚合教师 Dashboard 数据。
     *
     * @param userId 登录用户 id(必须 role=teacher;非教师抛 403)
     * @return 7 字段聚合 VO
     */
    AppTeacherDashboardSummaryRespVO getSummary(Long userId);

}
