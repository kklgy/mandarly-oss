-- 20260520_173000_teacher_profile_draft_status.sql
-- 目的:
--   1) 教师注册后先进入 draft(未提交)状态,避免新账号一登录即显示"资料已提交"。
--   2) 教师显式点击"提交审核"后才从 draft/rejected 进入 pending,进入 admin 待审队列。
-- 适用范围:
--   - 已存在的 pending 但未填写档案且未上传任何资质的教师,回退为 draft。
--   - 已经上传资质或已有档案内容的 pending 教师保留 pending,避免误撤真实待审申请。
-- 回滚:
--   - 需要手工将 draft 改回 pending 并恢复默认值;不建议自动回滚,会重新引入误提示。

SET NAMES utf8mb4;

ALTER TABLE teacher_profile
  MODIFY COLUMN audit_status VARCHAR(16) NOT NULL DEFAULT 'draft' COMMENT 'draft/pending/approved/rejected';

ALTER TABLE teacher_qualification
  MODIFY COLUMN doc_type VARCHAR(32) NOT NULL COMMENT 'id_card/passport/degree_cert/teaching_cert/english_cert/experience_proof';

UPDATE teacher_profile p
SET p.audit_status = 'draft',
    p.updater = '20260520_173000_teacher_profile_draft_status',
    p.update_time = NOW()
WHERE p.audit_status = 'pending'
  AND p.reject_reason IS NULL
  AND p.audited_at IS NULL
  AND p.audited_by IS NULL
  AND (p.intro IS NULL OR p.intro = '')
  AND p.expertise IS NULL
  AND (p.accent IS NULL OR p.accent = '')
  AND p.languages IS NULL
  AND p.years_experience IS NULL
  AND (p.intro_video_url IS NULL OR p.intro_video_url = '')
  AND NOT EXISTS (
    SELECT 1
    FROM teacher_qualification q
    WHERE q.user_id = p.user_id
      AND q.deleted = 0
  );
