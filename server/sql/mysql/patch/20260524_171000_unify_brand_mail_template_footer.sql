-- 20260524_171000_unify_brand_mail_template_footer.sql
-- Purpose:
--   D21: unify Mandarly business mail templates with a branded HTML shell and
--   company footer. Teacher audit and booking-created templates were already
--   full HTML; this patch closes the remaining auth / payment / withdrawal gaps.
-- Notes:
--   1) SQL-updated templates may need Redis `mail_template:*` cache eviction on prod.
--   2) This patch is idempotent: plain text templates are wrapped only once, and
--      existing HTML footers are only upgraded when the company footer is absent.
-- Rollback:
--   Restore affected `system_mail_template` rows from backup, or rerun earlier
--   template seed patches for the specific domain.

SET NAMES utf8mb4;

START TRANSACTION;

-- Payment collapsed base templates were inserted after the earlier D7.1 wrapper.
-- Wrap any remaining plain-text Mandarly payment / withdrawal templates.
UPDATE `system_mail_template`
SET
    title = REPLACE(REPLACE(`title`, 'Mandarly ·', 'Mandarly -'), '[TODO ar] ', ''),
    content = CONCAT(
        '<!doctype html><html><body style="margin:0;background:#f6f3ed;padding:24px;font-family:Arial,sans-serif;color:#1f2933;">',
        '<div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #eadfce;border-radius:16px;overflow:hidden;">',
        '<div style="padding:28px 32px;border-bottom:1px solid #f0e6d8;background:#fffaf0;">',
        '<div style="font-size:24px;font-weight:700;color:#1f2933;">Mandarly</div>',
        '<div style="margin-top:8px;font-size:14px;color:#6b7280;">1-on-1 Mandarin speaking practice</div>',
        '</div>',
        '<div style="padding:32px;font-size:15px;line-height:1.8;color:#4b5563;">',
        '<div style="white-space:pre-line;">',
        REPLACE(REPLACE(REPLACE(`content`, '[TODO ar] ', ''), '\r\n', '\n'), '\n', '<br>'),
        '</div>',
        '</div>',
        '<div style="padding:20px 32px;border-top:1px solid #f0e6d8;font-size:12px;line-height:1.7;color:#9ca3af;">',
        'This is an automated email. Please do not reply.<br>',
        '© 2026 MANDARLY TECHNOLOGY LIMITED / 曼德勵科技有限公司. All rights reserved.<br>',
        'RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong',
        '</div></div></body></html>'
    ),
    updater = '20260524_171000_mail_brand_footer',
    update_time = NOW()
WHERE deleted = b'0'
  AND content NOT LIKE '<!doctype html>%'
  AND (code LIKE 'mandarly_payment_%' OR code LIKE 'mandarly_withdrawal_%');

-- Upgrade already-HTML auth / Mandarly templates that still have the short old footer.
UPDATE `system_mail_template`
SET
    title = REPLACE(REPLACE(`title`, 'Mandarly ·', 'Mandarly -'), '[TODO ar] ', ''),
    content = REPLACE(
        REPLACE(`content`, '[TODO ar] ', ''),
        'This is an automated email. Please do not reply.<br>© Mandarly',
        CONCAT(
            'This is an automated email. Please do not reply.<br>',
            '© 2026 MANDARLY TECHNOLOGY LIMITED / 曼德勵科技有限公司. All rights reserved.<br>',
            'RM 131, 1/F, 143 Wai Yip Street, Kwun Tong, Hong Kong'
        )
    ),
    updater = '20260524_171000_mail_brand_footer',
    update_time = NOW()
WHERE deleted = b'0'
  AND content LIKE '<!doctype html>%'
  AND (code LIKE 'auth-mail-%' OR code LIKE 'mandarly_%')
  AND content NOT LIKE '%MANDARLY TECHNOLOGY LIMITED%'
  AND content NOT LIKE '%曼德勵科技有限公司%';

COMMIT;
