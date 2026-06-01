-- D19 Phase B Task B0 - 2026-05-18(2026-05-18 严格幂等版)
-- 目的: seed Mandarly COS 香港 master 配置,取代 ruoyi-vue-pro.sql 自带的 10 条占位示例
--       上线后所有 fileService.upload() 走这条 master(D19 教师资质材料上传 / 头像 / 凭证图)
-- 幂等: 重复执行结果一致 — 关掉旧 master + 确保 Mandarly COS 行存在且为唯一 master
-- 注意: prod 上线前必须把 accessKey / accessSecret 通过 admin 后台或环境变量改成真实凭证
--       占位串: PLACEHOLDER_REPLACE_WITH_ENV(memory secrets - 禁止真凭证入库)
--       重跑此 patch 不会覆盖 accessKey/accessSecret(step 2 用 NOT EXISTS 跳过)

SET NAMES utf8mb4;

-- 1) 取消所有现有 master,但 *排除* Mandarly COS 行(避免重跑时把已升 master 的本行误关)
UPDATE infra_file_config SET master = b'0'
  WHERE master = b'1'
    AND NOT (name = 'Mandarly COS 香港(prod)' AND storage = 20);

-- 2) 插入 Mandarly COS 香港(prod)配置,若已存在则跳过(strict idempotent)
--    storage=20 表示 S3 兼容;domain 给出公网域(私有 bucket,真实下载靠后端签名 URL)
INSERT INTO infra_file_config (name, storage, remark, master, config, creator, updater)
SELECT
  'Mandarly COS 香港(prod)',
  20,
  'D19 seed: prod cutover 前请在 admin 后台或 env 改填真实 accessKey/accessSecret;私有 bucket,签名 URL 下载',
  b'1',
  '{"@class":"com.mandarly.boot.module.infra.framework.file.core.client.s3.S3FileClientConfig","endpoint":"https://cos.ap-hongkong.myqcloud.com","domain":"https://mandarly-demo-bucket.cos.ap-hongkong.myqcloud.com","bucket":"mandarly-demo-bucket","accessKey":"OSS_ACCESS_KEY_EXAMPLE","accessSecret":"OSS_ACCESS_SECRET_EXAMPLE","region":"ap-hongkong","enablePathStyleAccess":false,"enablePublicAccess":false}',
  'd19-migration',
  'd19-migration'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM infra_file_config
    WHERE name = 'Mandarly COS 香港(prod)' AND storage = 20
  );

-- 3) 兜底:若 Mandarly COS 行存在但 master=0(历史误关),升回 master=1
UPDATE infra_file_config SET master = b'1'
  WHERE name = 'Mandarly COS 香港(prod)' AND storage = 20 AND master = b'0';

-- 4) 登记到 mandarly_patch_log(INSERT IGNORE 让重跑静默幂等)
INSERT IGNORE INTO mandarly_patch_log (patch_file, executed_at, executor, note)
VALUES (
  '20260518_120000_seed_file_config_mandarly_cos.sql',
  NOW(),
  'd19-migration',
  'D19 Phase B Task B0 - seed Mandarly COS 香港作为 file_config master,prod 切真凭证'
);
