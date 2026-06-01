-- Add the four collapsed M4 payment mail template codes used by backend services.
-- Scope: system_mail_template 221-224.
-- Rollback: mark these four rows as deleted if the backend returns to locale-suffixed template codes.

SET NAMES utf8mb4;

INSERT INTO system_mail_template
  (id, name, code, account_id, nickname, title, content, params, status, remark, creator, updater)
VALUES
(221, 'Payment purchase success', 'mandarly_payment_success', 1, 'Mandarly',
 'Mandarly · Package purchase confirmed',
 'Hi there!\n\nYou have successfully purchased the Mandarly package "{packageName}".\n\nAmount paid: {currency} {amountPaid}\n\nYou can now book your 1-on-1 Mandarin lessons:\nhttp://localhost:3001/my/packages\n\n-- Mandarly Team',
 '["packageName","amountPaid","currency","locale"]', 0, 'M4 collapsed base template: purchase success', 'system', 'system'),
(222, 'Payment refund processed', 'mandarly_payment_refund', 1, 'Mandarly',
 'Mandarly · Refund processed',
 'Hi there!\n\nYour refund for the Mandarly package "{packageName}" has been processed.\n\nRefund amount: {currency} {finalAmount}\n\nPlease allow 5-10 business days for the funds to appear on your original payment method.\n\nIf you have any questions, contact: support@mandarly.com\n\n-- Mandarly Team',
 '["finalAmount","currency","packageName","locale"]', 0, 'M4 collapsed base template: refund processed', 'system', 'system'),
(223, 'Teacher refund deduction', 'mandarly_payment_teacher_deduct', 1, 'Mandarly',
 'Mandarly · Income adjustment notice',
 'Dear {teacherName},\n\nA refund was requested by student {studentEmail} (reason: {reason}). The following amount has been deducted from your earnings:\n\nDeducted: USD {deductedAmountUsd}\n\nIf you have any concerns, please contact the operations team.\n\n-- Mandarly Team',
 '["teacherName","deductedAmountUsd","studentEmail","reason","locale"]', 0, 'M4 collapsed base template: teacher refund deduction', 'system', 'system'),
(224, 'Referral reward granted', 'mandarly_payment_referral_reward', 1, 'Mandarly',
 'Mandarly · Referral reward granted',
 'Hi {referrerName}!\n\nCongratulations! Your friend {refereeEmail} just bought a package. You have earned 1 free trial class as a reward.\n\nCheck your packages:\nhttp://localhost:3001/my/packages\n\n-- Mandarly Team',
 '["referrerName","refereeEmail","locale"]', 0, 'M4 collapsed base template: referral reward', 'system', 'system')
ON DUPLICATE KEY UPDATE
  name        = VALUES(name),
  code        = VALUES(code),
  account_id  = VALUES(account_id),
  nickname    = VALUES(nickname),
  title       = VALUES(title),
  content     = VALUES(content),
  params      = VALUES(params),
  status      = VALUES(status),
  remark      = VALUES(remark),
  updater     = VALUES(updater),
  update_time = NOW();
