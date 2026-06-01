-- Fix mojibake caused by early SQL imports that used a latin1 mysql client.
-- Scope: auth mail templates 101-106 and edu admin menus 3001-3020.
-- Rollback: restore from database backup if needed; this patch only replaces corrupted display text.

SET NAMES utf8mb4;

UPDATE system_mail_template
SET name = '认证-注册验证码-中文',
    title = 'Mandarly - 注册验证码',
    content = '欢迎注册 Mandarly 中文培训!\n\n您的注册验证码是:{code}\n\n5 分钟内有效,请勿告知他人。\n\n-- Mandarly Team',
    updater = 'migration',
    update_time = NOW()
WHERE id = 101 AND code = 'auth-mail-register';

UPDATE system_mail_template
SET name = '认证-注册验证码-英文',
    title = 'Mandarly - Registration Code',
    content = 'Welcome to Mandarly!\n\nYour registration code is: {code}\n\nValid for 5 minutes. Do not share with anyone.\n\n-- Mandarly Team',
    updater = 'migration',
    update_time = NOW()
WHERE id = 102 AND code = 'auth-mail-register-en';

UPDATE system_mail_template
SET name = '认证-登录验证码-中文',
    title = 'Mandarly - 登录验证码',
    content = '您的 Mandarly 登录验证码是:{code}\n\n5 分钟内有效。如非本人操作请忽略。\n\n-- Mandarly Team',
    updater = 'migration',
    update_time = NOW()
WHERE id = 103 AND code = 'auth-mail-login';

UPDATE system_mail_template
SET name = '认证-登录验证码-英文',
    title = 'Mandarly - Login Code',
    content = 'Your Mandarly login code is: {code}\n\nValid for 5 minutes. Ignore this email if you did not request it.\n\n-- Mandarly Team',
    updater = 'migration',
    update_time = NOW()
WHERE id = 104 AND code = 'auth-mail-login-en';

UPDATE system_mail_template
SET name = '认证-密码重置-中文',
    title = 'Mandarly - 密码重置',
    content = '您正在重置 Mandarly 账户密码。\n\n验证码:{code}\n\n5 分钟内有效。如非本人操作请立即修改密码或联系客服。\n\n-- Mandarly Team',
    updater = 'migration',
    update_time = NOW()
WHERE id = 105 AND code = 'auth-mail-reset';

UPDATE system_mail_template
SET name = '认证-密码重置-英文',
    title = 'Mandarly - Password Reset',
    content = 'You requested a Mandarly password reset.\n\nCode: {code}\n\nValid for 5 minutes. If this was not you, change your password immediately or contact support.\n\n-- Mandarly Team',
    updater = 'migration',
    update_time = NOW()
WHERE id = 106 AND code = 'auth-mail-reset-en';

UPDATE system_menu SET name = '教育业务', updater = 'migration', update_time = NOW() WHERE id = 3001;
UPDATE system_menu SET name = '教师审核', updater = 'migration', update_time = NOW() WHERE id = 3002;
UPDATE system_menu SET name = '查询教师档案', updater = 'migration', update_time = NOW() WHERE id = 3003;
UPDATE system_menu SET name = '审核教师档案', updater = 'migration', update_time = NOW() WHERE id = 3004;
UPDATE system_menu SET name = '套餐管理', updater = 'migration', update_time = NOW() WHERE id = 3005;
UPDATE system_menu SET name = '查询套餐', updater = 'migration', update_time = NOW() WHERE id = 3006;
UPDATE system_menu SET name = '新建套餐', updater = 'migration', update_time = NOW() WHERE id = 3007;
UPDATE system_menu SET name = '更新套餐', updater = 'migration', update_time = NOW() WHERE id = 3008;
UPDATE system_menu SET name = '删除套餐', updater = 'migration', update_time = NOW() WHERE id = 3009;
UPDATE system_menu SET name = '订单看板', updater = 'migration', update_time = NOW() WHERE id = 3010;
UPDATE system_menu SET name = '查询订单', updater = 'migration', update_time = NOW() WHERE id = 3011;
UPDATE system_menu SET name = '支付管理', updater = 'migration', update_time = NOW() WHERE id = 3012;
UPDATE system_menu SET name = '查询支付', updater = 'migration', update_time = NOW() WHERE id = 3013;
UPDATE system_menu SET name = '退款管理', updater = 'migration', update_time = NOW() WHERE id = 3014;
UPDATE system_menu SET name = '查询退款', updater = 'migration', update_time = NOW() WHERE id = 3015;
UPDATE system_menu SET name = '审核退款', updater = 'migration', update_time = NOW() WHERE id = 3016;
UPDATE system_menu SET name = '教师收入', updater = 'migration', update_time = NOW() WHERE id = 3017;
UPDATE system_menu SET name = '查询收入', updater = 'migration', update_time = NOW() WHERE id = 3018;
UPDATE system_menu SET name = '推荐记录', updater = 'migration', update_time = NOW() WHERE id = 3019;
UPDATE system_menu SET name = '查询推荐', updater = 'migration', update_time = NOW() WHERE id = 3020;
