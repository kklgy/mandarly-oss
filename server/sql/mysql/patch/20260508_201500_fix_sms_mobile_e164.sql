-- M2.5-β:system_sms_code / system_sms_log mobile 列扩长支持 E.164
-- 原若依标配 varchar(11) 仅适配国内 11 位号码;Mandarly 跨境 + 国际 (+1xxxxxxxxxx, +85298xxxxxxx 等)
-- 需要 16 位以容纳「+ 区号 + 号码」最大场景;扩到 20 留缓冲
-- 旧数据(11 位国内号)兼容,无需迁移

ALTER TABLE system_sms_code MODIFY COLUMN mobile VARCHAR(20) NOT NULL COMMENT '手机号(E.164,可带 + 与最长 15 位)';
ALTER TABLE system_sms_log  MODIFY COLUMN mobile VARCHAR(20) NOT NULL COMMENT '手机号(E.164,可带 + 与最长 15 位)';
