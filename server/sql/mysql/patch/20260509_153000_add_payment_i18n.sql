-- =============================================================================
-- Patch:M4 支付/退款/推荐/套餐 i18n 文案(2026-05-09)
-- =============================================================================
-- 覆盖:payment.* / refund.* / referral.* / package.*(补充)
-- 语言:zh-CN / zh-TW / en / ar(ar 为初版占位 [TODO ar],二期由阿语校对补全)
-- 共约 50 key × 4 = 200 行
-- 依赖:i18n_message 表已存在(mandarly.sql §05)
-- =============================================================================

INSERT INTO `i18n_message` (`code`, `locale`, `text`, `category`) VALUES

-- ============ payment.* (20 keys) ============
('payment.button.checkout',         'zh-CN', '立即购买',                           'payment'),
('payment.button.checkout',         'zh-TW', '立即購買',                           'payment'),
('payment.button.checkout',         'en',    'Buy Now',                            'payment'),
('payment.button.checkout',         'ar',    '[TODO ar] اشتري الآن',               'payment'),

('payment.button.retry',            'zh-CN', '重试',                               'payment'),
('payment.button.retry',            'zh-TW', '重試',                               'payment'),
('payment.button.retry',            'en',    'Retry',                              'payment'),
('payment.button.retry',            'ar',    '[TODO ar] إعادة المحاولة',            'payment'),

('payment.processing.title',        'zh-CN', '支付处理中',                         'payment'),
('payment.processing.title',        'zh-TW', '支付處理中',                         'payment'),
('payment.processing.title',        'en',    'Processing payment',                 'payment'),
('payment.processing.title',        'ar',    '[TODO ar] جاري معالجة الدفع',        'payment'),

('payment.processing.tip',          'zh-CN', '请稍候,我们正在确认你的支付...',    'payment'),
('payment.processing.tip',          'zh-TW', '請稍候,我們正在確認你的支付...',    'payment'),
('payment.processing.tip',          'en',    'Please wait, confirming your payment...', 'payment'),
('payment.processing.tip',          'ar',    '[TODO ar] يرجى الانتظار...',         'payment'),

('payment.success.title',           'zh-CN', '支付成功!',                          'payment'),
('payment.success.title',           'zh-TW', '支付成功！',                         'payment'),
('payment.success.title',           'en',    'Payment successful!',                'payment'),
('payment.success.title',           'ar',    '[TODO ar] تم الدفع بنجاح!',          'payment'),

('payment.success.message',         'zh-CN', '套餐已到账,开始预约你的课程吧!',    'payment'),
('payment.success.message',         'zh-TW', '套餐已到帳,開始預約你的課程吧！',   'payment'),
('payment.success.message',         'en',    'Your package is ready. Book your first class now!', 'payment'),
('payment.success.message',         'ar',    '[TODO ar] تم تفعيل الباقة. احجز درسك الأول الآن!', 'payment'),

('payment.cancel.title',            'zh-CN', '支付已取消',                         'payment'),
('payment.cancel.title',            'zh-TW', '支付已取消',                         'payment'),
('payment.cancel.title',            'en',    'Payment cancelled',                  'payment'),
('payment.cancel.title',            'ar',    '[TODO ar] تم إلغاء الدفع',           'payment'),

('payment.cancel.message',          'zh-CN', '你取消了支付流程,套餐未购买。',     'payment'),
('payment.cancel.message',          'zh-TW', '你取消了支付流程,套餐未購買。',     'payment'),
('payment.cancel.message',          'en',    'You cancelled the payment. No charge was made.', 'payment'),
('payment.cancel.message',          'ar',    '[TODO ar] لم يتم خصم أي مبلغ.',     'payment'),

('payment.cancel.retry',            'zh-CN', '重新购买',                           'payment'),
('payment.cancel.retry',            'zh-TW', '重新購買',                           'payment'),
('payment.cancel.retry',            'en',    'Try again',                          'payment'),
('payment.cancel.retry',            'ar',    '[TODO ar] حاول مرة أخرى',            'payment'),

('payment.cancel.back',             'zh-CN', '返回我的套餐',                       'payment'),
('payment.cancel.back',             'zh-TW', '返回我的套餐',                       'payment'),
('payment.cancel.back',             'en',    'My packages',                        'payment'),
('payment.cancel.back',             'ar',    '[TODO ar] باقاتي',                   'payment'),

('payment.expired.title',           'zh-CN', '支付处理中,请稍后查看',             'payment'),
('payment.expired.title',           'zh-TW', '支付處理中,請稍後查看',             'payment'),
('payment.expired.title',           'en',    'Payment is being processed',         'payment'),
('payment.expired.title',           'ar',    '[TODO ar] جارٍ معالجة الدفع',       'payment'),

('payment.amount.usd',              'zh-CN', '退款金额 (USD)',                     'payment'),
('payment.amount.usd',              'zh-TW', '退款金額 (USD)',                     'payment'),
('payment.amount.usd',              'en',    'Amount (USD)',                       'payment'),
('payment.amount.usd',              'ar',    '[TODO ar] المبلغ (USD)',              'payment'),

('payment.discount.applied',        'zh-CN', '推荐码折扣已应用',                   'payment'),
('payment.discount.applied',        'zh-TW', '推薦碼折扣已套用',                   'payment'),
('payment.discount.applied',        'en',    'Referral discount applied',          'payment'),
('payment.discount.applied',        'ar',    '[TODO ar] تم تطبيق خصم الإحالة',    'payment'),

('payment.status.pending',          'zh-CN', '待支付',                             'payment'),
('payment.status.pending',          'zh-TW', '待支付',                             'payment'),
('payment.status.pending',          'en',    'Pending',                            'payment'),
('payment.status.pending',          'ar',    '[TODO ar] في الانتظار',              'payment'),

('payment.status.paid',             'zh-CN', '已支付',                             'payment'),
('payment.status.paid',             'zh-TW', '已支付',                             'payment'),
('payment.status.paid',             'en',    'Paid',                               'payment'),
('payment.status.paid',             'ar',    '[TODO ar] مدفوع',                    'payment'),

('payment.status.failed',           'zh-CN', '支付失败',                           'payment'),
('payment.status.failed',           'zh-TW', '支付失敗',                           'payment'),
('payment.status.failed',           'en',    'Failed',                             'payment'),
('payment.status.failed',           'ar',    '[TODO ar] فشل الدفع',                'payment'),

('payment.status.expired',          'zh-CN', '已过期',                             'payment'),
('payment.status.expired',          'zh-TW', '已過期',                             'payment'),
('payment.status.expired',          'en',    'Expired',                            'payment'),
('payment.status.expired',          'ar',    '[TODO ar] منتهي الصلاحية',           'payment'),

('payment.status.refunded',         'zh-CN', '已退款',                             'payment'),
('payment.status.refunded',         'zh-TW', '已退款',                             'payment'),
('payment.status.refunded',         'en',    'Refunded',                           'payment'),
('payment.status.refunded',         'ar',    '[TODO ar] تم الاسترداد',             'payment'),

('payment.status.partial_refunded', 'zh-CN', '部分退款',                           'payment'),
('payment.status.partial_refunded', 'zh-TW', '部分退款',                           'payment'),
('payment.status.partial_refunded', 'en',    'Partially refunded',                 'payment'),
('payment.status.partial_refunded', 'ar',    '[TODO ar] استرداد جزئي',             'payment'),

('payment.viewMyPackages',          'zh-CN', '查看我的套餐',                       'payment'),
('payment.viewMyPackages',          'zh-TW', '查看我的套餐',                       'payment'),
('payment.viewMyPackages',          'en',    'View my packages',                   'payment'),
('payment.viewMyPackages',          'ar',    '[TODO ar] عرض باقاتي',               'payment'),

-- ============ refund.* (15 keys) ============
('refund.apply.button',             'zh-CN', '申请退款',                           'refund'),
('refund.apply.button',             'zh-TW', '申請退款',                           'refund'),
('refund.apply.button',             'en',    'Request refund',                     'refund'),
('refund.apply.button',             'ar',    '[TODO ar] طلب استرداد',              'refund'),

('refund.apply.title',              'zh-CN', '申请退款',                           'refund'),
('refund.apply.title',              'zh-TW', '申請退款',                           'refund'),
('refund.apply.title',              'en',    'Request a refund',                   'refund'),
('refund.apply.title',              'ar',    '[TODO ar] طلب استرداد',              'refund'),

('refund.apply.reason.label',       'zh-CN', '退款原因',                           'refund'),
('refund.apply.reason.label',       'zh-TW', '退款原因',                           'refund'),
('refund.apply.reason.label',       'en',    'Reason for refund',                  'refund'),
('refund.apply.reason.label',       'ar',    '[TODO ar] سبب الاسترداد',            'refund'),

('refund.apply.reason.placeholder', 'zh-CN', '请说明退款原因(至少 10 个字)',       'refund'),
('refund.apply.reason.placeholder', 'zh-TW', '請說明退款原因（至少 10 個字）',     'refund'),
('refund.apply.reason.placeholder', 'en',    'Please explain your reason (min 10 chars)', 'refund'),
('refund.apply.reason.placeholder', 'ar',    '[TODO ar] الرجاء توضيح السبب',      'refund'),

('refund.apply.reason.min10',       'zh-CN', '退款原因至少需要 10 个字',           'refund'),
('refund.apply.reason.min10',       'zh-TW', '退款原因至少需要 10 個字',           'refund'),
('refund.apply.reason.min10',       'en',    'Reason must be at least 10 characters', 'refund'),
('refund.apply.reason.min10',       'ar',    '[TODO ar] يجب أن يكون السبب 10 أحرف على الأقل', 'refund'),

('refund.apply.success',            'zh-CN', '退款申请已提交,等待审核',           'refund'),
('refund.apply.success',            'zh-TW', '退款申請已提交,等待審核',           'refund'),
('refund.apply.success',            'en',    'Refund request submitted, pending review', 'refund'),
('refund.apply.success',            'ar',    '[TODO ar] تم تقديم طلب الاسترداد',   'refund'),

('refund.apply.error',              'zh-CN', '提交失败,请稍后重试',               'refund'),
('refund.apply.error',              'zh-TW', '提交失敗,請稍後重試',               'refund'),
('refund.apply.error',              'en',    'Submission failed, please try again', 'refund'),
('refund.apply.error',              'ar',    '[TODO ar] فشل التقديم',              'refund'),

('refund.myList.title',             'zh-CN', '我的退款记录',                       'refund'),
('refund.myList.title',             'zh-TW', '我的退款記錄',                       'refund'),
('refund.myList.title',             'en',    'My refund requests',                 'refund'),
('refund.myList.title',             'ar',    '[TODO ar] طلبات الاسترداد الخاصة بي', 'refund'),

('refund.myList.empty',             'zh-CN', '暂无退款记录',                       'refund'),
('refund.myList.empty',             'zh-TW', '暫無退款記錄',                       'refund'),
('refund.myList.empty',             'en',    'No refund requests yet',             'refund'),
('refund.myList.empty',             'ar',    '[TODO ar] لا توجد طلبات استرداد',    'refund'),

('refund.status.pending',           'zh-CN', '审核中',                             'refund'),
('refund.status.pending',           'zh-TW', '審核中',                             'refund'),
('refund.status.pending',           'en',    'Pending',                            'refund'),
('refund.status.pending',           'ar',    '[TODO ar] قيد المراجعة',             'refund'),

('refund.status.approved',          'zh-CN', '已批准',                             'refund'),
('refund.status.approved',          'zh-TW', '已批准',                             'refund'),
('refund.status.approved',          'en',    'Approved',                           'refund'),
('refund.status.approved',          'ar',    '[TODO ar] تمت الموافقة',             'refund'),

('refund.status.refunded',          'zh-CN', '已到账',                             'refund'),
('refund.status.refunded',          'zh-TW', '已到帳',                             'refund'),
('refund.status.refunded',          'en',    'Refunded',                           'refund'),
('refund.status.refunded',          'ar',    '[TODO ar] تم الاسترداد',             'refund'),

('refund.status.rejected',          'zh-CN', '已拒绝',                             'refund'),
('refund.status.rejected',          'zh-TW', '已拒絕',                             'refund'),
('refund.status.rejected',          'en',    'Rejected',                           'refund'),
('refund.status.rejected',          'ar',    '[TODO ar] مرفوض',                    'refund'),

('refund.status.failed',            'zh-CN', '退款失败',                           'refund'),
('refund.status.failed',            'zh-TW', '退款失敗',                           'refund'),
('refund.status.failed',            'en',    'Failed',                             'refund'),
('refund.status.failed',            'ar',    '[TODO ar] فشل الاسترداد',            'refund'),

('refund.auditNote.label',          'zh-CN', '审核备注',                           'refund'),
('refund.auditNote.label',          'zh-TW', '審核備註',                           'refund'),
('refund.auditNote.label',          'en',    'Admin note',                         'refund'),
('refund.auditNote.label',          'ar',    '[TODO ar] ملاحظة المشرف',            'refund'),

-- ============ referral.* (10 keys) ============
('referral.title',                  'zh-CN', '我的推荐战绩',                       'referral'),
('referral.title',                  'zh-TW', '我的推薦戰績',                       'referral'),
('referral.title',                  'en',    'My referral stats',                  'referral'),
('referral.title',                  'ar',    '[TODO ar] إحصائيات الإحالة',         'referral'),

('referral.code.your',              'zh-CN', '我的推荐码',                         'referral'),
('referral.code.your',              'zh-TW', '我的推薦碼',                         'referral'),
('referral.code.your',              'en',    'My referral code',                   'referral'),
('referral.code.your',              'ar',    '[TODO ar] رمز الإحالة الخاص بي',     'referral'),

('referral.code.copy',              'zh-CN', '复制',                               'referral'),
('referral.code.copy',              'zh-TW', '複製',                               'referral'),
('referral.code.copy',              'en',    'Copy',                               'referral'),
('referral.code.copy',              'ar',    '[TODO ar] نسخ',                      'referral'),

('referral.code.copied',            'zh-CN', '推荐码已复制',                       'referral'),
('referral.code.copied',            'zh-TW', '推薦碼已複製',                       'referral'),
('referral.code.copied',            'en',    'Code copied!',                       'referral'),
('referral.code.copied',            'ar',    '[TODO ar] تم نسخ الرمز',             'referral'),

('referral.stat.invited',           'zh-CN', '已邀请人数',                         'referral'),
('referral.stat.invited',           'zh-TW', '已邀請人數',                         'referral'),
('referral.stat.invited',           'en',    'Friends invited',                    'referral'),
('referral.stat.invited',           'ar',    '[TODO ar] الأصدقاء المدعوون',        'referral'),

('referral.stat.reward',            'zh-CN', '累计奖励课次',                       'referral'),
('referral.stat.reward',            'zh-TW', '累計獎勵課次',                       'referral'),
('referral.stat.reward',            'en',    'Free classes earned',                'referral'),
('referral.stat.reward',            'ar',    '[TODO ar] الدروس المجانية المكتسبة', 'referral'),

('referral.discount.applied',       'zh-CN', '推荐码折扣已应用',                   'referral'),
('referral.discount.applied',       'zh-TW', '推薦碼折扣已套用',                   'referral'),
('referral.discount.applied',       'en',    'Referral discount applied',          'referral'),
('referral.discount.applied',       'ar',    '[TODO ar] تم تطبيق خصم الإحالة',    'referral'),

('referral.tip.invite',             'zh-CN', '邀请好友注册并购买套餐,你将获得免费体验课', 'referral'),
('referral.tip.invite',             'zh-TW', '邀請好友註冊並購買套餐,你將獲得免費體驗課', 'referral'),
('referral.tip.invite',             'en',    'Invite a friend to buy a package and earn a free class', 'referral'),
('referral.tip.invite',             'ar',    '[TODO ar] ادع صديقاً لشراء باقة واحصل على درس مجاني', 'referral'),

('referral.tip.firstOrder',         'zh-CN', '好友首单可享折扣',                   'referral'),
('referral.tip.firstOrder',         'zh-TW', '好友首單可享折扣',                   'referral'),
('referral.tip.firstOrder',         'en',    'Your friend gets a discount on their first order', 'referral'),
('referral.tip.firstOrder',         'ar',    '[TODO ar] يحصل صديقك على خصم في طلبه الأول', 'referral'),

('referral.tip.howItWorks',         'zh-CN', '好友首次购买套餐后,双方均得奖励',   'referral'),
('referral.tip.howItWorks',         'zh-TW', '好友首次購買套餐後,雙方均得獎勵',   'referral'),
('referral.tip.howItWorks',         'en',    'Both you and your friend are rewarded after first purchase', 'referral'),
('referral.tip.howItWorks',         'ar',    '[TODO ar] يحصل كلاكما على مكافأة بعد أول عملية شراء', 'referral'),

-- ============ package.*(补充 5 keys) ============
('package.buyNow',                  'zh-CN', '立即购买',                           'package'),
('package.buyNow',                  'zh-TW', '立即購買',                           'package'),
('package.buyNow',                  'en',    'Buy now',                            'package'),
('package.buyNow',                  'ar',    '[TODO ar] اشتري الآن',               'package'),

('package.weeklyCount',             'zh-CN', '每周课次',                           'package'),
('package.weeklyCount',             'zh-TW', '每週課次',                           'package'),
('package.weeklyCount',             'en',    'Classes / week',                     'package'),
('package.weeklyCount',             'ar',    '[TODO ar] حصص / أسبوع',              'package'),

('package.validityWeeks',           'zh-CN', '总课次',                             'package'),
('package.validityWeeks',           'zh-TW', '總課次',                             'package'),
('package.validityWeeks',           'en',    'Total classes',                      'package'),
('package.validityWeeks',           'ar',    '[TODO ar] إجمالي الحصص',             'package'),

('package.priceUsd',                'zh-CN', '价格',                               'package'),
('package.priceUsd',                'zh-TW', '價格',                               'package'),
('package.priceUsd',                'en',    'Price',                              'package'),
('package.priceUsd',                'ar',    '[TODO ar] السعر',                    'package'),

('package.referralCode.placeholder','zh-CN', '输入推荐码(可选)',                   'package'),
('package.referralCode.placeholder','zh-TW', '輸入推薦碼（選填）',                 'package'),
('package.referralCode.placeholder','en',    'Referral code (optional)',           'package'),
('package.referralCode.placeholder','ar',    '[TODO ar] رمز الإحالة (اختياري)',    'package')

ON DUPLICATE KEY UPDATE
  `text`     = VALUES(`text`),
  `category` = VALUES(`category`),
  `updater`  = 'system';
