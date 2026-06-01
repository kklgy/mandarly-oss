-- =============================================================================
-- Patch:M2.5 OAuth2 客户端注册 app-client
-- =============================================================================
-- 来源:docs/progress/M2.5-登录改造.md §2.4
-- 影响:system_oauth2_client 新增一行 client_id=app-client(供 C 端 app 使用)
-- 可重入:ON DUPLICATE KEY UPDATE
-- 回滚:DELETE FROM system_oauth2_client WHERE client_id='app-client'
-- =============================================================================

INSERT INTO system_oauth2_client
  (id, client_id, secret, name, status,
   access_token_validity_seconds, refresh_token_validity_seconds,
   redirect_uris, authorized_grant_types, scopes, auto_approve_scopes,
   logo, description, authorities, resource_ids, additional_information,
   creator, updater)
VALUES
  (10, 'app-client', '', 'Mandarly App', 0,
   1800, 2592000,
   '["http://localhost:3001"]',
   '["password","refresh_token","sms","email","social_login"]',
   '["user.read","user.write"]',
   '["user.read","user.write"]',
   '', 'C 端 app 客户端(学生/教师)', '[]', '[]', '{}',
   'system', 'system')
ON DUPLICATE KEY UPDATE
  name=VALUES(name),
  description=VALUES(description),
  access_token_validity_seconds=VALUES(access_token_validity_seconds),
  refresh_token_validity_seconds=VALUES(refresh_token_validity_seconds),
  authorized_grant_types=VALUES(authorized_grant_types),
  scopes=VALUES(scopes),
  updater='system';
