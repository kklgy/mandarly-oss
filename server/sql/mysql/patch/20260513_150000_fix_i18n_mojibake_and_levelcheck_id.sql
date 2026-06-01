-- Patch: 修复 i18n_message 历史种子乱码 + 记录等级测试主键回填代码修复
-- Date: 2026-05-13
-- Scope:
--   1) i18n_message.text 中已按错误编码写入的 UTF-8 文案。
--   2) level_check_submission.id 仍为原 auto_increment 结构,代码层显式 IdType.AUTO。
-- Notes:
--   - 仅处理明显 mojibake 的文本,避免二次转换已正确文案。
--   - 不修改表结构;表与列已是 utf8mb4。

UPDATE i18n_message
SET text = CONVERT(BINARY CONVERT(text USING latin1) USING utf8mb4)
WHERE deleted = 0
  AND text REGEXP '[ÃÂÄÅÆÇÈÉÑÒÓÔÕÙØÐåæèéçð]';
