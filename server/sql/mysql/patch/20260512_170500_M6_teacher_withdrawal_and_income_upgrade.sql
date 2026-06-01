-- ============================================================
-- M6 教师收入分成 + 提现审核:数据库升级
-- 上游 spec: docs/product/M6-spec.md §2.1
-- 上游 design: docs/database/04-teacher-income-withdrawal.md v1.2
--
-- 一个 patch 做 3 件事(事务执行):
--   (a) teacher_income 升级到 v1.2 — 加 status / currency / frozen_until /
--       related_income_id / description + 新索引;frozen_until 回填 + NOT NULL
--   (b) teacher_balance 升级 — frozen_usd → frozen_t7_usd(rename);
--       加 pending / currency / version(乐观锁)
--   (c) teacher_withdrawal 新建 — 提现申请单(状态机 + AES-GCM 加密 payee_info)
--
-- 注意:
--   - prod 业务数据 = 0,迁移 0 风险
--   - DDL 在 MySQL 是 auto-commit 的,BEGIN/COMMIT 仅表达执行意图
-- ============================================================

BEGIN;

-- ============================================================
-- (a) teacher_income 升级 v1.2
-- ============================================================

ALTER TABLE teacher_income
  ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'frozen' COMMENT 'frozen/available/reverted',
  ADD COLUMN currency VARCHAR(8) NOT NULL DEFAULT 'USD',
  ADD COLUMN frozen_until DATETIME(3) NULL COMMENT '解冻时刻 = settled_at + 7d',
  ADD COLUMN related_income_id BIGINT UNSIGNED NULL COMMENT 'refund_deduct 时关联原 normal 行',
  ADD COLUMN description VARCHAR(256) NULL,
  ADD INDEX idx_teacher_status_frozen (teacher_id, status, frozen_until);

-- 回填:无条件执行(空表 = no-op,有遗留行 = 兜底)
UPDATE teacher_income SET frozen_until = DATE_ADD(settled_at, INTERVAL 7 DAY) WHERE frozen_until IS NULL;

-- 之后把 frozen_until 改为 NOT NULL(分两步,避免 ALTER 与 NULL 数据冲突)
ALTER TABLE teacher_income
  MODIFY COLUMN frozen_until DATETIME(3) NOT NULL COMMENT '解冻时刻';

-- ============================================================
-- (b) teacher_balance 升级
--   - frozen_usd → frozen_t7_usd (rename)
--   - 加 pending / currency / version(乐观锁)
-- ============================================================

ALTER TABLE teacher_balance
  CHANGE COLUMN frozen_usd frozen_t7_usd DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'T+7 未解冻',
  ADD COLUMN pending DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '提现 in-flight',
  ADD COLUMN currency VARCHAR(8) NOT NULL DEFAULT 'USD',
  ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁';

-- ============================================================
-- (c) teacher_withdrawal 新建
--   - 状态机 pending → approved → paid / rejected / failed
--   - payee_info: AES-256-GCM 密文(Base64),格式 v1:{iv}:{ct_with_tag}
--   - 索引按 spec §2.1 (c)
-- ============================================================

CREATE TABLE teacher_withdrawal (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  teacher_id BIGINT UNSIGNED NOT NULL,
  amount DECIMAL(12, 2) NOT NULL,
  currency VARCHAR(8) NOT NULL DEFAULT 'USD',
  payee_info VARCHAR(2048) NOT NULL COMMENT 'AES-256-GCM 密文(Base64),格式 v1:{iv}:{ct_with_tag}',
  payee_method VARCHAR(32) DEFAULT NULL COMMENT 'wechat/alipay/paypal/bank_card/other',
  status VARCHAR(16) NOT NULL DEFAULT 'pending' COMMENT 'pending/approved/paid/rejected/failed',
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  audited_by BIGINT DEFAULT NULL,
  audited_at DATETIME DEFAULT NULL,
  reject_reason VARCHAR(512) DEFAULT NULL,
  paid_by BIGINT DEFAULT NULL,
  paid_at DATETIME DEFAULT NULL,
  paid_proof VARCHAR(512) DEFAULT NULL COMMENT '打款凭证截图 URL(COS)',
  paid_remark VARCHAR(256) DEFAULT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NOT NULL DEFAULT '',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_teacher_status_applied (teacher_id, status, applied_at),
  KEY idx_status_applied (status, applied_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师提现申请';

COMMIT;
