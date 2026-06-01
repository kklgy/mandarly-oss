-- =============================================================================
-- Patch:水平测试 i18n_message 文案(2026-05-06)
-- =============================================================================
-- 来源:`docs/product/level-check-recommendation-v1.md` §3.1
-- 影响:i18n_message 表,共 88 条(4 题 + 18 选项)× 4 语言(en/zh-CN/zh-TW/ar)
-- 依赖:先执行 20260506_103000_add_levelcheck_tables.sql
-- 阿拉伯语为初版,精度有限,二期请人校对
-- =============================================================================

-- 题干
INSERT INTO `i18n_message` (`code`, `locale`, `text`, `category`) VALUES
('level_check.q1.text', 'en',    'What''s your current Mandarin level?',          'level_check'),
('level_check.q1.text', 'zh-CN', '您目前的中文水平?',                              'level_check'),
('level_check.q1.text', 'zh-TW', '您目前的中文水平?',                              'level_check'),
('level_check.q1.text', 'ar',    'ما هو مستواك الحالي في الماندرين؟',             'level_check'),

('level_check.q2.text', 'en',    'What''s your main goal?',                       'level_check'),
('level_check.q2.text', 'zh-CN', '您的主要学习目标?',                              'level_check'),
('level_check.q2.text', 'zh-TW', '您的主要學習目標?',                              'level_check'),
('level_check.q2.text', 'ar',    'ما هو هدفك الأساسي؟',                          'level_check'),

('level_check.q3.text', 'en',    'How often would you like to learn?',            'level_check'),
('level_check.q3.text', 'zh-CN', '您希望多久上一次课?',                            'level_check'),
('level_check.q3.text', 'zh-TW', '您希望多久上一次課?',                            'level_check'),
('level_check.q3.text', 'ar',    'كم مرة ترغب في الدراسة؟',                       'level_check'),

('level_check.q4.text', 'en',    'Who is the learner?',                           'level_check'),
('level_check.q4.text', 'zh-CN', '学习者是谁?',                                   'level_check'),
('level_check.q4.text', 'zh-TW', '學習者是誰?',                                   'level_check'),
('level_check.q4.text', 'ar',    'من هو المتعلم؟',                                'level_check');

-- Q1 选项(complete_beginner / some_basics / simple_conversation / fairly_fluent)
INSERT INTO `i18n_message` (`code`, `locale`, `text`, `category`) VALUES
('level_check.q1.opt.complete_beginner',   'en',    'Complete beginner',                'level_check'),
('level_check.q1.opt.complete_beginner',   'zh-CN', '完全零基础',                       'level_check'),
('level_check.q1.opt.complete_beginner',   'zh-TW', '完全零基礎',                       'level_check'),
('level_check.q1.opt.complete_beginner',   'ar',    'مبتدئ تمامًا',                     'level_check'),

('level_check.q1.opt.some_basics',         'en',    'Some basics (know a few words)',   'level_check'),
('level_check.q1.opt.some_basics',         'zh-CN', '学过一点(认识几个字)',            'level_check'),
('level_check.q1.opt.some_basics',         'zh-TW', '學過一點(認識幾個字)',            'level_check'),
('level_check.q1.opt.some_basics',         'ar',    'بعض الأساسيات (أعرف بضع كلمات)',  'level_check'),

('level_check.q1.opt.simple_conversation', 'en',    'Can hold simple conversations',    'level_check'),
('level_check.q1.opt.simple_conversation', 'zh-CN', '能简单对话',                       'level_check'),
('level_check.q1.opt.simple_conversation', 'zh-TW', '能簡單對話',                       'level_check'),
('level_check.q1.opt.simple_conversation', 'ar',    'يمكنني إجراء محادثات بسيطة',       'level_check'),

('level_check.q1.opt.fairly_fluent',       'en',    'Fairly fluent',                    'level_check'),
('level_check.q1.opt.fairly_fluent',       'zh-CN', '较流利',                           'level_check'),
('level_check.q1.opt.fairly_fluent',       'zh-TW', '較流利',                           'level_check'),
('level_check.q1.opt.fairly_fluent',       'ar',    'طلاقة جيدة نسبيًا',                'level_check');

-- Q2 选项
INSERT INTO `i18n_message` (`code`, `locale`, `text`, `category`) VALUES
('level_check.q2.opt.career',       'en',    'Career / promotion',                      'level_check'),
('level_check.q2.opt.career',       'zh-CN', '求职 / 晋升',                              'level_check'),
('level_check.q2.opt.career',       'zh-TW', '求職 / 晉升',                              'level_check'),
('level_check.q2.opt.career',       'ar',    'العمل / الترقية',                           'level_check'),

('level_check.q2.opt.business',     'en',    'Business communication',                  'level_check'),
('level_check.q2.opt.business',     'zh-CN', '商务沟通',                                 'level_check'),
('level_check.q2.opt.business',     'zh-TW', '商務溝通',                                 'level_check'),
('level_check.q2.opt.business',     'ar',    'التواصل التجاري',                          'level_check'),

('level_check.q2.opt.travel',       'en',    'Travel / daily life',                     'level_check'),
('level_check.q2.opt.travel',       'zh-CN', '旅行 / 日常生活',                          'level_check'),
('level_check.q2.opt.travel',       'zh-TW', '旅行 / 日常生活',                          'level_check'),
('level_check.q2.opt.travel',       'ar',    'السفر / الحياة اليومية',                  'level_check'),

('level_check.q2.opt.hsk_exam',     'en',    'HSK / YCT exam',                          'level_check'),
('level_check.q2.opt.hsk_exam',     'zh-CN', 'HSK / YCT 考试',                            'level_check'),
('level_check.q2.opt.hsk_exam',     'zh-TW', 'HSK / YCT 考試',                            'level_check'),
('level_check.q2.opt.hsk_exam',     'ar',    'اختبار HSK / YCT',                         'level_check'),

('level_check.q2.opt.for_kids',     'en',    'For my kids',                             'level_check'),
('level_check.q2.opt.for_kids',     'zh-CN', '给孩子学',                                 'level_check'),
('level_check.q2.opt.for_kids',     'zh-TW', '給孩子學',                                 'level_check'),
('level_check.q2.opt.for_kids',     'ar',    'لأطفالي',                                  'level_check'),

('level_check.q2.opt.just_for_fun', 'en',    'Just for fun',                            'level_check'),
('level_check.q2.opt.just_for_fun', 'zh-CN', '兴趣爱好',                                 'level_check'),
('level_check.q2.opt.just_for_fun', 'zh-TW', '興趣愛好',                                 'level_check'),
('level_check.q2.opt.just_for_fun', 'ar',    'للمتعة فقط',                               'level_check');

-- Q3 选项
INSERT INTO `i18n_message` (`code`, `locale`, `text`, `category`) VALUES
('level_check.q3.opt.one_per_week', 'en',    '1 class / week',                          'level_check'),
('level_check.q3.opt.one_per_week', 'zh-CN', '每周 1 节',                                 'level_check'),
('level_check.q3.opt.one_per_week', 'zh-TW', '每週 1 節',                                 'level_check'),
('level_check.q3.opt.one_per_week', 'ar',    'حصة واحدة في الأسبوع',                     'level_check'),

('level_check.q3.opt.two_per_week', 'en',    '2 classes / week',                        'level_check'),
('level_check.q3.opt.two_per_week', 'zh-CN', '每周 2 节',                                 'level_check'),
('level_check.q3.opt.two_per_week', 'zh-TW', '每週 2 節',                                 'level_check'),
('level_check.q3.opt.two_per_week', 'ar',    'حصتان في الأسبوع',                         'level_check'),

('level_check.q3.opt.intensive',    'en',    'Intensive (3+ classes / week)',           'level_check'),
('level_check.q3.opt.intensive',    'zh-CN', '强化集训(每周 3+ 节)',                   'level_check'),
('level_check.q3.opt.intensive',    'zh-TW', '強化集訓(每週 3+ 節)',                   'level_check'),
('level_check.q3.opt.intensive',    'ar',    'مكثف (3+ حصص في الأسبوع)',                'level_check'),

('level_check.q3.opt.not_sure',     'en',    'Not sure yet',                            'level_check'),
('level_check.q3.opt.not_sure',     'zh-CN', '还不确定',                                 'level_check'),
('level_check.q3.opt.not_sure',     'zh-TW', '還不確定',                                 'level_check'),
('level_check.q3.opt.not_sure',     'ar',    'لست متأكدًا بعد',                          'level_check');

-- Q4 选项
INSERT INTO `i18n_message` (`code`, `locale`, `text`, `category`) VALUES
('level_check.q4.opt.myself',   'en',    'Myself',                                      'level_check'),
('level_check.q4.opt.myself',   'zh-CN', '我自己',                                       'level_check'),
('level_check.q4.opt.myself',   'zh-TW', '我自己',                                       'level_check'),
('level_check.q4.opt.myself',   'ar',    'نفسي',                                         'level_check'),

('level_check.q4.opt.child',    'en',    'My child (under 12)',                         'level_check'),
('level_check.q4.opt.child',    'zh-CN', '我的孩子(< 12 岁)',                          'level_check'),
('level_check.q4.opt.child',    'zh-TW', '我的孩子(< 12 歲)',                          'level_check'),
('level_check.q4.opt.child',    'ar',    'طفلي (أقل من 12 سنة)',                        'level_check'),

('level_check.q4.opt.teen',     'en',    'Teen (12-18)',                                'level_check'),
('level_check.q4.opt.teen',     'zh-CN', '青少年(12-18 岁)',                           'level_check'),
('level_check.q4.opt.teen',     'zh-TW', '青少年(12-18 歲)',                           'level_check'),
('level_check.q4.opt.teen',     'ar',    'مراهق (12-18)',                               'level_check'),

('level_check.q4.opt.multiple', 'en',    'Multiple family members',                     'level_check'),
('level_check.q4.opt.multiple', 'zh-CN', '多位家庭成员',                                 'level_check'),
('level_check.q4.opt.multiple', 'zh-TW', '多位家庭成員',                                 'level_check'),
('level_check.q4.opt.multiple', 'ar',    'عدة أفراد من الأسرة',                         'level_check');

-- =============================================================================
-- patch end:88 条 i18n_message
-- =============================================================================
