-- =============================================================================
-- Patch: N2 加纳上线 — support_contact market=GH WhatsApp 占位
-- =============================================================================
-- 来源:`docs/research/ghana-launch-feasibility.md` §三 改动清单 项 3
-- 决策:project owner 2026-05-06 加纳列入第一批上线市场(与香港 / 迪拜并列,合入 PRD-v4.3)
-- 工程文档:`docs/progress/N2-加纳上线.md`
--
-- 范围说明:
--   原可行性评估文档项 3 提到"system_dict_data 字典补 GH 项",但摸清后发现项目
--   原本没有 country / market / timezone 字典(时区前端走 Intl.supportedValuesOf,
--   PhoneInput 组件未建,market 字段直接用枚举值不依赖 dict)。任何 dict 项都没有
--   消费方,本 patch 不新建 dict 结构 — 真上线流程做注册流 / admin §A11 时再建。
--
-- 本 patch 影响:
--   support_contact:插入 1 行 market=GH 的 WhatsApp 配置占位
--     WhatsApp 号留 'TBD'(等project owner申请加纳市场专用 WhatsApp Business 商业号,
--     到位后单独 patch UPDATE display_text + link_url + is_active=1)
--
-- 幂等保证:
--   support_contact 用 (market, channel_type, deleted) 三元判断 + INSERT ... SELECT WHERE NOT EXISTS
--   重跑 exit code 0,数据不重复
--
-- 不可回滚:数据插入。删除会影响线上(若已上线消费此行配置)。
--
-- 关联 PRD:§A11 私域联系方式配置 / §6 首发市场
-- =============================================================================

-- WhatsApp 号留 'TBD'(等project owner申请加纳市场专用 WhatsApp Business 商业号)
-- 上线流程:号码到位后用单独 patch UPDATE display_text + link_url + is_active=1
-- link_url 留空,前端展示 display_text 即可(避免 wa.me/TBD 导致 404)

INSERT INTO `support_contact`
  (`market`, `channel_type`, `display_text`, `link_url`, `image_url`, `sort`, `is_active`, `creator`, `updater`)
SELECT 'GH', 'whatsapp', 'WhatsApp (Ghana) — TBD', NULL, NULL, 0, 0,
       'system', 'system'
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM `support_contact`
  WHERE `market` = 'GH' AND `channel_type` = 'whatsapp' AND `deleted` = 0
);

-- patch end:1 row support_contact(首次执行)
