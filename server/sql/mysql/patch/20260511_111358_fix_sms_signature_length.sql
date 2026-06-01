-- N1 留尾 #3 修复:system_sms_channel.signature 字段长度从 VARCHAR(12) 扩到 VARCHAR(32)
--
-- 背景:
--   prod N1.4 fresh init 时应用 20260508_220000_add_domestic_sms_channel.sql 报
--   "Data too long for column 'signature' at row 1"。原 ruoyi-vue-pro schema
--   把 signature 限到 VARCHAR(12),覆盖中文签名(如「曼德勵」「【Mandarly】」)
--   勉强够,但放不下国际/混合签名(如「Mandarly Hong Kong」18 字符 / 未来多品牌)。
--   prod 当时手动 ALTER 临时扩容跑通,git 里需要永久补丁兜底新建库。
--
-- 决策:
--   腾讯云 ISMS 中文签名 2-12 字符,英文/混合允许更长;扩到 32 覆盖实战 + 留余量,
--   避免再吃一次扩字段。NOT NULL / utf8mb4 / COMMENT 与原 schema 对齐。
--
-- 幂等性:
--   MySQL ALTER COLUMN MODIFY 同定义重跑不报错(InnoDB 本身去重),可重入。
--
-- 不可回滚:扩字段安全,但缩回去会截断已存入数据,本 patch 不提供 down。

ALTER TABLE `system_sms_channel`
  MODIFY COLUMN `signature` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '短信签名';
