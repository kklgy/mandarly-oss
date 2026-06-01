/**
 * Mandarly 在线中文培训 · edu 业务模块
 * <p>
 * 包含 12 个子域(对照 PRD-v4 §4 + 数据库设计 docs/database/):
 * <ul>
 *   <li>teacher  — 教师档案 / 资质审核 / 排课</li>
 *   <li>student  — 学生档案</li>
 *   <li>package  — 课程套餐 + 学生套餐</li>
 *   <li>schedule — 教师排课 + 时段例外</li>
 *   <li>booking  — 预约下单 + 订单状态机</li>
 *   <li>classroom — LCIC 课堂集成</li>
 *   <li>payment  — Stripe 接入 + Webhook</li>
 *   <li>income   — 教师收入结算</li>
 *   <li>withdrawal — 提现申请 + 审核</li>
 *   <li>referral — 推荐码</li>
 *   <li>review   — 课后评价</li>
 *   <li>notification — 邮件 / 短信 / 站内信</li>
 * </ul>
 *
 * @author Mandarly Team
 */
package com.mandarly.boot.module.edu;
