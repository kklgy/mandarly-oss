---
status: active
type: source_of_truth
domain: frontend-style
---

# Mandarly 前端 UI 风格规范 v1

> 版本: v1.1 · 2026-05-06
> 适用: app/(用户端)+ admin/(管理端)
> 维护: OPC,所有前端会话以本文为契约
> 上游: PRD-v4 §13(品牌)/§4.3(学生端)/§4.8(多语言 RTL)/`docs/brand/deliverables-v1/`
> 配套视觉样板: `视觉参考/DESIGN-mandarly-v1.md`(组件级视觉决策,与本文双向对齐)
> 配套技能: `.claude/skills/ui-ux-pro-max/`(用户私有 skill,前端动工前自动加载)

## 0. 文档定位与变更原则

- **唯一的前端视觉契约**——admin、app、邮件模板、社交物料的色 / 字 / 间距 / 组件选择必须出自本文
- 与 PRD 冲突时,以 **PRD 为业务源、本文为实现源**;若实现需偏离规范,必须先改本文(留版本号)再改代码
- 任何新增页面在 PR / 提交前需对照 §10 Checklist
- 本文版本演进:小修补丁直接覆盖并加变更日志条目;重大调整(如换主色 / 换组件库)开 v2 文档

## 1. 品牌主轴

### 1.1 主色

| token | 值 | 用途 |
|---|---|---|
| `$brand-primary` | `#FFB627` | 主按钮、链接、强调标签、品牌强化 |
| `$brand-primary-deep` | `#FFA60A` | hover / active / 渐变深端 |
| `$brand-primary-light` | `#FFC34D` | 渐变浅端、辅助强调 |
| `$brand-primary-soft` | `#FFF6E5` | 卡片留白底、信息提示底色 |

主色渐变:`linear-gradient(135deg, #FFC34D 0%, #FFA60A 100%)`(品牌 hero / banner / 关键 CTA)

### 1.2 中性色

| token | 值 | 用途 |
|---|---|---|
| `$color-text-primary` | `#1a1a1a` | 标题、正文 |
| `$color-text-secondary` | `#595959` | 次要正文、副标题 |
| `$color-text-tertiary` | `#8c8c8c` | 占位符、辅助说明、时间戳 |
| `$color-text-disabled` | `#bfbfbf` | 禁用文字 |
| `$color-text-inverse` | `#ffffff` | ink / 深色实底按钮上的文字 |
| `$btn-primary-text` | `#1a1a1a` | 主色按钮文字,黄底深字保证 WCAG 对比度 |
| `$color-border` | `#e8e8e8` | 卡片 / 输入框边框 |
| `$color-border-light` | `#f0f0f0` | 表格分隔线 |
| `$color-divider` | `#f5f5f5` | 区块分隔条 |
| `$color-bg-page` | `#fafafa` | 页面背景(默认) |
| `$color-bg-card` | `#ffffff` | 卡片背景 |
| `$color-bg-mask` | `rgba(0,0,0,.45)` | 弹窗遮罩 |

### 1.3 状态色

| token | 值 | 场景 |
|---|---|---|
| `$color-success` | `#52c41a` | 成功提示、已支付、上课中 |
| `$color-warning` | `#faad14` | 即将过期、待评价、已申请提现 |
| `$color-error` | `#ff4d4f` | 失败、退款、缺席、删除 |
| `$color-info` | `#1890ff` | 中性提示(避免与主色 #FFB627 视觉冲突,不滥用) |

### 1.4 禁止

- ❌ `.vue / .ts / .js` 中出现 `#FFB627` `#FFA60A` 等品牌色十六进制字面量
- ❌ 重新发明色板(如自创 `#FFAA22`)
- ❌ 用 `rgba(255, 182, 39, 0.5)` 替代 `var(--color-primary)` + 透明度滤镜
- ❌ Element Plus 默认蓝 `#409EFF` 残留(已被 element-overrides 全部覆盖,新页面也不该出现)

### 1.5 视觉参考来源(v1.1 加项)

本文规范的是**工程契约**(token / 组件库 / 编码纪律)。组件具体长什么样、各状态、字体阶梯映射,在配套视觉样板里:

- **主样板**: `视觉参考/DESIGN-mandarly-v1.md`——Mandarly 适配版,用 Google Stitch DESIGN.md 范式,frontmatter 颜色 / 字体 / 组件 token 机器可读,AI 协作首选。
- **范式镜像**: `视觉参考/DESIGN-airbnb-原版.md`——Airbnb 原版,只读,追溯设计哲学时翻;**禁止**按 Mandarly 主色 / 字体改这份。
- 详细分层与维护规则: `README.md`、`视觉参考/README.md`

**对接关系**:
- 本文的 token(§1-§4)是 brand.scss 的真值源
- 视觉样板的 frontmatter token 必须**和本文一致**——任何一边改 token,另一边同步
- 视觉样板新增组件 token(如 `$radius-pill` 32px)未在本文出现的,本文 §4 同步补一行

**AI 协作 prompt 约定**:让 Claude / Codex 写前端时,头一行写
> 写之前先读 `docs/frontend/ui-style-guide-v1.md`(工程契约) + `docs/frontend/visual-reference/DESIGN-mandarly-v1.md`(视觉样板),按这两份落地。

不要直接让 AI 看 Airbnb 原版——主色 / 字体 / RTL 都没翻译过来,容易跑偏。

## 2. 字体

```scss
$font-family-base:
  -apple-system, BlinkMacSystemFont,
  'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
  'Noto Sans SC', 'Noto Sans Arabic',
  Helvetica, Arial, sans-serif;

$font-family-heading-en: Georgia, 'Times New Roman', Times, serif;
```

- 中文 `PingFang SC`(macOS/iOS) → `Microsoft YaHei`(Windows) → `Noto Sans SC`(Linux/缺字时)
- 英文标题(品牌调性,首页 hero / 邮件 hero / 营销页大标题)用 `Georgia` 衬线斜体
- 阿语字符浏览器自动选 `Noto Sans Arabic`(已在 fallback 链中)
- ❌ 业务页面里写 `font-family: ...`;统一从 `body` 继承

### 2.1 字号阶梯

| token | 值 | 场景 |
|---|---|---|
| `$font-size-xs` | 12px | 时间戳、辅助说明 |
| `$font-size-sm` | 13px | 表格小字、标签 |
| `$font-size-base` | 14px | 正文 |
| `$font-size-md` | 16px | H5 正文、按钮文字 |
| `$font-size-lg` | 18px | 卡片标题、表单 label |
| `$font-size-xl` | 20px | 区块标题 |
| `$font-size-2xl` | 24px | 页面标题 |
| `$font-size-3xl` | 30px | 营销页大标题 |
| `$font-size-4xl` | 38px | Hero 主标题 |

行高:`$line-height-tight: 1.2`(标题) / `$line-height-base: 1.5`(正文) / `$line-height-loose: 1.75`(长文)

## 3. 间距与栅格

- **基础单位 8px**(4 是细微补偿)
- 间距 token: `$spacing-1: 4px` `$spacing-2: 8px` `$spacing-3: 12px` `$spacing-4: 16px` `$spacing-5: 20px` `$spacing-6: 24px` `$spacing-8: 32px` `$spacing-10: 40px` `$spacing-12: 48px` `$spacing-16: 64px`
- 卡片内边距:H5 用 `$spacing-4` (16px) / PC 用 `$spacing-6` (24px)
- 卡片外边距:H5 用 `$spacing-3` (12px) / PC 用 `$spacing-4` (16px)
- 区块之间间距:`$spacing-8` ~ `$spacing-12` (32-48px)
- 表单 label 与控件间:`$spacing-2` (8px)
- ❌ `padding: 13px` `margin: 7px` —— 必须用 token

## 4. 圆角与阴影

| token | 值 | 场景 |
|---|---|---|
| `$radius-sm` | 4px | 标签、徽章 |
| `$radius-base` | 8px | 输入框、按钮、表格 |
| `$radius-lg` | 12px | 卡片(默认) |
| `$radius-card` | 14px | 教师卡 / 套餐卡(参考 Airbnb property card) |
| `$radius-xl` | 16px | 大型卡片 / Modal |
| `$radius-pill` | 32px | 中等 pill(category strip / pill button) |
| `$radius-full` | 9999px | 头像、Pill 标签、搜索 orb |

| token | 值 | 场景 |
|---|---|---|
| `$shadow-sm` | 1px 偏移 4% 黑 | 列表项 hover |
| `$shadow-base` | 2px 偏移 6% 黑 | 卡片(默认) |
| `$shadow-md` | 4px 偏移 8% 黑 | 卡片 hover、Dropdown |
| `$shadow-lg` | 8px 偏移 12% 黑 | Modal、Drawer |
| `$shadow-brand` | 主色光晕 32% | 主按钮 hover、关键 CTA |
| `$ring-focus` | 主色 16% 3px 光晕 | 输入框 / 可聚焦元素的 focus ring |
| `$scrim-modal` | 黑 50% | 模态遮罩 |

## 5. 响应式断点(PC + H5 同源)

```scss
$bp-mobile:  480px;
$bp-tablet:  768px;   // ★ H5/PC 视觉切换分水岭
$bp-laptop:  1024px;
$bp-desktop: 1280px;
```

- **<768px** —— H5 视觉,Vant 组件,单列卡片,底栏导航
- **≥768px** —— PC 视觉,Element Plus 组件,栅格 2-4 列,顶栏导航
- 770-820px 这种边界尺寸视觉上参考 PC 布局(只是更窄),不切回 H5
- 教师列表 / 详情 / 套餐购买等核心页**必须有 PC + H5 双视觉验收**

## 6. 组件库选型纪律(v1.2 · 2026-05-09 修订)

> **修订背景**:UX审计与优化-v1.md § 4.1 决策 1 拍板**路线 A**——全栈 Element Plus,Vant 仅作高频移动端补充。原 v1.0 / v1.1 的"<768 用 Vant"规则废止。

| 端 / 视图 | 主选 | 何时用 |
|---|---|---|
| **admin/web-antd** | Ant Design Vue 4 + vben 内置 form/table | 所有 admin 业务页 |
| **app/ 全部视口** | Element Plus(主) | 默认所有交互组件(Form / Dialog / Table / DatePicker / Select / Tabs 等) |
| **app/ H5 高频移动场景**(可选 Vant 补充) | Vant 4(辅) | 下列**特定场景**才允许引入,避免逐页全改: ① 底部 Action Sheet(订单取消、币种切换);② 全屏 Popup(图片预览);③ 时间滚轮选择器(<768 排课时段挑选);④ 下拉刷新 / 触底加载(列表页面) |
| 跨视口 | 用 SCSS @media + 结构调整 | H5 视图通过 `@media (max-width: bp-tablet)` 调整 EP 组件外观(全屏 Dialog、加大触控区) |

### 6.1 禁忌

- ❌ 同一页面同时用 `ElDialog` 与 `VanDialog`(同类组件二选一)
- ❌ admin 端引入 Element Plus / Vant(vben 是 Ant Design Vue 体系)
- ❌ 用 Vant 在 PC ≥ 768 视图下渲染(Vant 是 H5 设计,PC 比例失调)
- ❌ 自封装一套低质量组件库替代成熟方案
- ❌ 因为"H5 体验更好"就把整页从 EP 切到 Vant——除非命中 § 6 表格里列出的 4 类高频场景,否则 H5 体验问题通过 SCSS / 自定义组件解决,不引入 Vant

### 6.2 H5 上 Element Plus 体验优化的 SCSS 套路(路线 A 必备)

由于 H5 上不再默认切 Vant,EP 组件的 H5 适配靠 SCSS:

| EP 组件 | <768 视图改造 |
|---|---|
| `el-dialog` | 全屏化:`width: 100% !important; margin: 0; height: 100vh; border-radius: 0`(底弹则:`top: auto; bottom: 0; transform: translateY(0)`) |
| `el-form` `el-form-item` | label-position="top" + size="large";触控区高度 ≥ 44px |
| `el-tabs` | sticky top + 横向滚动 + indicator 移动动效 |
| `el-input` | 字号 16px(避免 iOS Safari 自动缩放) |
| `el-select` | H5 触发后改用 `<el-drawer placement="bottom">` 半屏选项 |
| `el-pagination` | H5 隐藏,改"加载更多"按钮 / 触底加载 |

### 6.2 共识封装(随时间沉淀)

预期下一阶段沉淀的薄封装组件(用户端 `app/src/components/`):

- `<TeacherCard>` —— 卡片化教师展示(PC + H5 自适应)
- `<PriceTag>` —— 多币种价格显示(自动 HKD / USD / CNY 格式),支持未登录 `locked` 毛玻璃态;真实金额必须由后端按登录态返回,不能仅前端 blur
- `<ScheduleSlotPicker>` —— 时段选择(教师时区 → 学生时区转换)
- `<EmptyState>` —— 空态插画 + 文案 + CTA(品牌米色底)
- `<MandarlyButton>` —— 仅在主题需要超出 Element Plus / Vant 范围时使用

每个组件落规范文档(本文 §11 索引)+ Storybook(M3+ 评估)。

## 7. 阿语 RTL 专项(二期预留)

> 2026-05-24 project owner确认:一期只对外开放英文 / 简体中文 / 繁体中文。阿语代码、翻译包和 RTL 能力保留,但入口与 SEO 信号隐藏;二期开启前再做完整 RTL 视觉验收。

### 7.1 已就位的基础设施

- `app/src/i18n/index.js` 切语言时自动设 `<html dir="rtl">`
- `app/src/App.vue` Vant ConfigProvider 接收 `:rtl="rtl"`
- `dayjs` locale 同步切换

### 7.2 编码纪律

- 用 logical properties:
  - `margin-inline-start` 替代 `margin-left`
  - `margin-inline-end` 替代 `margin-right`
  - `padding-inline-start/end` 替代 `padding-left/right`
  - `text-align: start / end` 替代 `text-align: left / right`
- 方向性图标(箭头、返回、进度条)需 `[dir="rtl"] .arrow { transform: scaleX(-1); }`
- 脚本里判断方向用 `import { isRTL } from '@/i18n'`,例如轮播 / 抽屉滑出方向
- 数字默认阿拉伯数字 0-9(运营要求才切印度数字 ٠١٢٣)

### 7.3 验收

阿语二期开放前,每个新页面至少提供 **LTR(英文 + 中文) PC/H5 截图**;若页面改动涉及 logical properties、方向性图标、时间 / 金额显示或共享布局,仍需补一张 RTL smoke 截图避免二期债务扩大。以下穿帮模式必须避免:

- 头像 / 名字位置错乱
- 价格位置(应靠 inline-end);未登录价格锁定态不应露出真实金额或价格筛选 / 排序
- 进度条方向反了
- 抽屉 / Drawer 滑入方向不变(应自动镜像)
- 文本溢出方向不对(text-overflow: ellipsis 在 RTL 下也应在合适方向截断)

## 8. 多语言文案

### 8.1 落地规则

| 类型 | 落库点 | 维护 |
|---|---|---|
| **业务内容**(题目、套餐名、教师标签、邮件模板) | 后端 `i18n_message` 表 + Spring MessageSource | 后端 SQL patch + 通过 admin A10 / A13 维护 |
| **UI 文案**(按钮、表单 label、提示) | 前端 `app/src/i18n/locales/{en,zh-CN,zh-TW,ar}.js` | 一期启用 en / zh-CN / zh-TW;ar 保留隐藏并继续 key 对齐 |
| **用户生成内容**(教师介绍、评价) | 不翻译,显示原文 | PRD §4.8 决策 |

### 8.2 编码纪律

- 站点默认主语言为英文:无用户显式选择时初始 locale 必须为 `en`;用户手动切换后才写入 `mandarly_locale` 并记忆
- 一期启用语言白名单为 `en` / `zh-CN` / `zh-TW`;`ar.js` 继续打包但不在 `setLocale()` 与 UI 下拉中开放
- 模板里禁止硬编码业务文案,统一 `t('xxx.yyy.zzz')`
- key 命名:`page.section.element.attr`,如 `home.hero.title` `s1.filter.priceRange.label`
- 启用三语必须同步增删;ar key 保持同步以免二期开启时断裂
- 阿语入口隐藏,二期开放前需母语复核且不向阿联酋市场推广
- 文案不超过两行(表单 label 不超 12 字符 / 阿语视长度自适应)

## 9. 时区与时间显示

- 后端 UTC 存
- 前端按 `用户.timezone` 显示;优先级为后端 profile `timezone` → 浏览器 `Intl.DateTimeFormat().resolvedOptions().timeZone` → `UTC`,**不按 IP 定位**
- 登录 / 注册 / 刷新 profile 后必须同步 `profile.timezone` 到 `@/utils/datetime` 的本地缓存;退出登录 / 切账号必须清理,避免上个账号时区影响下个账号
- 用 `@/utils/datetime` 的 dayjs 封装,**禁止 `new Date()` 直接显示时间**
- 教师 / 学生时区独立:
  - 排课设置(教师端)用教师时区,DO 字段记录设置时时区
  - 预约日历(学生端)按学生时区渲染,后端做转换
  - 邮件 / 短信通知用收件人时区
- 显示格式由 dayjs locale 决定(已与 i18n 同步切换):
  - `zh-CN`: `2026年5月6日 14:30`
  - `en`: `May 6, 2026 2:30 PM`
  - `ar`: `٦ مايو ٢٠٢٦ ٢:٣٠ م`(运营审核后启用印度数字)

## 10. 页面动工 Checklist

每个新页面(或重大重构)在合并前必填:

```
[ ] 1. PRD 章节对照写入 PR 描述(§X.Y 标题)
[ ] 2. 后端接口路径 / VO 字段已确认,数据形态贴入 PR 描述
[ ] 3. i18n 文案 key 列表(4 语言均已新增)
[ ] 4. 方向性 CSS 全部为 logical properties / 已用 isRTL() 处理
[ ] 5. 新增 design-token 全部加到 brand.scss(若有)
[ ] 6. 组件库选型与本规范 §6 一致,未混用同类
[ ] 7. PC ≥768 + H5 <768 双视觉已设计 / 截图
[ ] 8. 时间字段已用 dayjs + 用户时区
[ ] 9. 金额字段已用多币种格式化函数
[ ] 10. 浏览器 console 无 warning(未知 CSS 变量、vue-i18n missing key 等)
[ ] 11. RTL 模式截图已对比无穿帮
[ ] 12. Demo 数据测试:特殊字符 / 长文本 / emoji 不溢出
```

## 11. 当前已沉淀的封装组件

> 第一次写本规范时,共享组件仅 `LanguageSwitcher`。后续每新增一个共享组件在此追加索引。

| 组件 | 路径 | 状态 | 说明 |
|---|---|---|---|
| LanguageSwitcher | `app/src/components/LanguageSwitcher.vue` | ✅ | 切语言下拉,顶栏 / 用户中心可放 |

## 12. 已知坑(避免重蹈)

1. `HomeView.vue` 用 `var(--mandarly-primary)` 但 brand.scss 暴露 `var(--color-primary)` —— 历史遗留命名不一致。新页面统一 `--color-*`。**修复**:本规范落地同时把 HomeView 改正(下一次提交)。
2. Element Plus 默认中文,英文场景需 `<el-config-provider :locale="enLocale">`,与 vue-i18n locale 切换联动。
3. Vant z-index 默认偏低(1000+),与 Element Plus Modal(2000+)叠加可能被压盖,需对照 brand.scss `$z-*` 层级表。
4. unplugin-vue-components 自动按需引入了 Element Plus 与 Vant,新增第三方组件需在 `vite.config.js` resolvers 加进来。
5. 跑 admin/ dev 时 `pnpm dev:antd` 会启动 5666 端口,与 `app/` 的 3001 不冲突;同机同时跑两个 dev 没问题。

## 12.1 配套审计文档(2026-05-09 加项)

本文是**工程契约**(规约 + 编码纪律,稳定低频迭代)。如果你想知道**当前 app/ 用户端实际有哪些页面 / 双端体验问题 / 哪些规范没落地 / 上线前需要按什么节奏修**——请读配套审计:

- `UX审计与优化-v1.md`(2026-05-09)——双端 UX 审计 + P0-P3 优化路线图。该文识别"规范要求 vs 代码现状"偏离(关键发现:Vant 在 20 个 view 里 0 处使用 / `app/src/layouts/` 空目录)、列出每页双端问题、给出全局 Layout / AppHeader / AppTabBar 的改造路线。

审计文档会随 P0 落地而更新,落完整轮后冻结归档,**不替代本文工程契约**。

## 12.2 教师身份顶栏 / 头像菜单切换规则(D16 · 2026-05-15)

Mandarly 一期不做同账号身份切换(D13),但教师登录后前端导航需与学生侧分流,避免给教师呈现获客入口。本节是 PRD-v4 §4.2 T0 在工程契约层的落地。

### 触发条件

```js
const isTeacher = computed(() => profile.value?.role === 'teacher' && isLoggedIn.value)
```

### 顶栏主导航(`AppHeader.vue` `NAV_ITEMS`)

| 身份 | 1 | 2 | 3 | 4 |
|---|---|---|---|---|
| 未登录 | 了解 Mandarly | 价格 | 学习资源 | 等级测试 |
| 学生 | 老师 | 价格 | 等级测试 | 我的课 |
| 教师 | 工作台 | 排课 | 订单 | 收入 |

- 顶栏主导航必须按 `教师 / 未登录 / 学生` 三态切换数组,不是模板内 v-if 单项隐藏(避免数组结构与 active 计算耦合错位)
- 未登录态必须保留 `/packages` 的 `Pricing` 入口;价格和金额明细继续按后端 `priceVisible=false` 做毛玻璃锁定,不能只靠前端隐藏真实金额。
- 教师 i18n key 复用 `teacherCenter.nav.{dashboard,schedule,orders,income}`,**不新造 `header.nav.teacher*` key**
- `activeNavKey` 计算属性须同步分支处理 `/teacher-center/*` 路径
- 教师身份下隐藏 `teachers / packages / levelCheck` 三项,路由仍可访问(不强拦)

### 头像下拉(`AppHeader.vue` `el-dropdown-menu`)

| 身份 | 顺序 |
|---|---|
| 学生 | 个人中心 / 我的套餐 / 我的课 / ──── / 退出登录 |
| 教师 | 教师中心 / 我的订单 / 我的收入 / 申请提现 / ──── / 个人资料 / 退出登录 |

- 教师下拉**不出现** `我的套餐 / 我的退款`(学生侧概念)
- "个人中心" / "个人资料" 用两个独立 i18n key(`header.menu.profile` / `header.menu.profileTeacher`),路由都指向 `/profile` 但文案区分
- "我的课" / "我的订单" 同理两个独立 i18n key(`header.menu.myOrders` / `header.menu.myOrdersTeacher`),路由分别指 `/orders` / `/teacher-center/orders`

### 教师 chip(`👨‍🏫 教师中心` 角色徽标)

**撤掉**。顶栏 NAV_ITEMS 已包含"工作台"作为第一项,chip 重复且抢视觉焦点。原 `app-header__role-chip*` CSS 选择器一并清掉,避免死代码。

### AppFooter(`AppFooter.vue`)

**不按身份切换**。footer 是公开页全局元素(关于 / 服务 / 联系 / 法律),与顶栏工作流入口语义不同。教师也可能从 footer 进入公司介绍 / 服务说明等公开页。footer 内既有 `header.nav.{teachers,packages,levelCheck}` i18n key 复用 OK,语义对齐"公开页 nav"而非"工作流 nav"。

**Follow Us 列(社交外链)**:第 5 列由 `app/src/config/socials.js` 的 `SOCIAL_LINKS` 数组**配置驱动**,`getActiveSocialLinks()` 仅渲染 `url` 非空的项;预置 Facebook / Instagram / X / YouTube / LinkedIn / WeChat 六个经典平台的 SVG path,当前仅 Instagram 有 url。**新增平台只需在该配置文件填 url,不要去改 AppFooter 模板/样式**。圆形 icon 按钮用 `--color-bg-strong` 底 + `--color-primary` hover + `--ring-focus` focus,无方向性 CSS,RTL 下整列对称镜像到最左,圆形 icon 自然不需要 scaleX(-1)。aria-label 用 i18n `footer.social.followOn` 模板,品牌名 `{name}` 直接用 `displayName`(品牌名不翻译)。

### 移动端 tabbar(`AppTabBar.vue`)

已落地无需改 — 通过 `route.path.startsWith('/teacher-center')` 切换学生套 / 教师套 4 槽位:

- 学生套:首页 / 老师 / 我的课 / 我
- 教师套:排课 / 订单 / 收入 / 我(进 `/teacher-center/*` 自动切换)

### 教师中心 Dashboard 数据源

`/teacher-center` 首页 4 数据卡(本周课次 / 本月收入 / 待结算 / 评价分)走单一聚合 endpoint:

```
GET /app-api/edu/teacher-center/dashboard/summary
```

后端按教师 `user.timezone`(UTC 兜底)计算周窗口(周一 00:00 ~ 周日 23:59)与月窗口。**前端禁止做时间窗口 filter**,所有口径以后端为准。M6 留下的"前端 filter + 简化口径"是 D16 P2 修补范围。

### 上线 Checklist(本节专项)

- [ ] 教师登录 PC:顶栏仅 4 项工作流;无 `老师/套餐/等级测试`;无教师 chip;头像下拉 7 项无"我的套餐"
- [ ] 教师登录 H5:顶栏仅 logo + 语言 + 头像;tabbar 进 `/teacher-center/*` 切教师套
- [ ] 学生登录 PC + H5:顶栏 / 下拉 / tabbar 全部回归无破坏
- [ ] 未登录 PC + H5:顶栏 3 项(无"我的课"),无回归
- [ ] 语言:一期入口仅 en / zh-CN / zh-TW;ar key 保留但不出现在用户下拉
- [ ] Dashboard:Network 面板只调一个 summary endpoint,不再有 balance / orders / teacher-stat 三次调用

## 13. 变更日志

- **v1.4 · 2026-05-18**:§12 AppFooter 段补 Follow Us 列(社交外链)指引——配置驱动 `app/src/config/socials.js`,预置 6 平台 SVG path,空 url 自动隐藏;Instagram 首批接入。
- **v1.3 · 2026-05-15**:加 §12.2 教师身份顶栏 / 头像菜单切换规则(D16,PRD §4.2 T0 工程契约落地)
- **v1.2 · 2026-05-09**:
  - § 6 组件库选型纪律重写——路线 A 落地(全栈 EP,Vant 仅作高频移动端 4 类场景补充)
  - § 6.2 新增 H5 上 EP 组件的 SCSS 套路表(全屏 Dialog / 大字号 input / sticky tabs / drawer select)
  - § 6.1 禁忌补一条:不允许"为了 H5 体验"整页切 Vant
- **v1.1 · 2026-05-06**:接入 `视觉参考/` 子目录(DESIGN.md 范式)。
  - 加 §1.5 视觉参考来源
  - §4 圆角补 `$radius-card` 14px(教师/套餐卡)+ `$radius-pill` 32px(中等 pill)
  - §4 阴影补 `$ring-focus`(主色 focus ring)+ `$scrim-modal`(模态遮罩)
  - brand.scss 同步补 `$brand-primary-disabled` / `$color-text-body` / `$color-border-strong` / `$color-bg-strong`
- **v1 · 2026-05-06**:首版,沉淀 `brand.scss` token 体系 + 组件库纪律 + RTL/i18n/时区规范 + Checklist。Codex by ui-ux-pro-max skill 第一次执行。

---

> 下次有任何前端任务,先读本文 §1-§9 + `视觉参考/DESIGN-mandarly-v1.md`,再对照 §10 Checklist 写代码。OPC 的纪律靠文档,不靠记性。
