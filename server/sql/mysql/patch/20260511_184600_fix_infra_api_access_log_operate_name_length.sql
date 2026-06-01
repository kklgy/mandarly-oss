-- patch:fix infra_api_access_log.operate_name varchar(50) → varchar(200)
-- 起因:2026-05-11 prod Google OAuth 真链路异步任务 createApiAccessLogAsync 报
--   "Data truncation: Data too long for column 'operate_name' at row 1"
-- 根因:`infra_api_access_log` 表来自上游 ruoyi-vue-pro mysql 老版 schema(varchar 50),
--       新版 ruoyi 已扩到 varchar 200(`@Operation(summary="...")` 中文描述常 > 50)
-- 影响:仅异步 access log 记录失败(@Async 吞异常),不阻塞业务;但污染日志 + access log 表 0 数据无法对账
-- prod 已 hot ALTER 2026-05-11 17:18(N1 后实测时直接改);本 patch 让所有环境对齐,新建库自动应用

ALTER TABLE infra_api_access_log MODIFY COLUMN operate_name VARCHAR(200);
