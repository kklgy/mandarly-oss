-- =============================================================================
-- Patch: 修复早期 latin1 客户端写入导致的短信渠道 / 模板 / 日志中文乱码
-- 影响:
--   - system_sms_channel.remark
--   - system_sms_template.content
--   - system_sms_log.template_content
-- 可重入:WHERE 仅匹配典型 mojibake 字符,二次执行不再命中
-- 回滚:无自动回滚;如需回滚请使用执行前备份
-- =============================================================================

SET NAMES utf8mb4;

UPDATE system_sms_channel
SET remark = CONVERT(CAST(CONVERT(remark USING latin1) AS BINARY) USING utf8mb4),
    updater = 'migration',
    update_time = NOW()
WHERE remark IS NOT NULL
  AND remark REGEXP '[ÃÂâæçèéå]';

UPDATE system_sms_template
SET content = CONVERT(CAST(CONVERT(content USING latin1) AS BINARY) USING utf8mb4),
    updater = 'migration',
    update_time = NOW()
WHERE content IS NOT NULL
  AND content REGEXP '[ÃÂâæçèéå]';

UPDATE system_sms_log
SET template_content = CONVERT(CAST(CONVERT(template_content USING latin1) AS BINARY) USING utf8mb4),
    updater = 'migration',
    update_time = NOW()
WHERE template_content IS NOT NULL
  AND template_content REGEXP '[ÃÂâæçèéå]';
