# Mandarly 用户端(app/)UX 审计与优化路线 v1 — 🧊 已冻结归档 2026-05-10

> 版本: v1 · 2026-05-09
> **冻结归档: 2026-05-10**(M5 用户端 UX 大改造完成,P0/P1/P2 全部落地)
> 范围: `app/`(用户端,响应式 PC + H5 同源)
> 维护: OPC,Claude / Codex 在做用户端 UI 优化时与本文双向对齐
> 上游契约: `UI风格规范-v1.md`(token / 组件库 / RTL / i18n / 时区)
> 视觉样板: `视觉参考/DESIGN-mandarly-v1.md`(组件级视觉决策)
> PRD 锚点: PRD-v4 §4.2 教师端 / §4.3 学生端 / §5 非功能性 / §13 品牌
>
> **本文是审计 + 路线图,不是规约**:本文识别"规范要求 vs 代码现状"的偏离、列出双端体验问题、给出 P0-P3 优化路线。
> **本文已冻结**:M5 改造把 P0/P1/P2 全部落地(6 commits 08ffb84..19c2ab8),实施跟踪 `docs/progress/M5-用户端UX大改造.md`。新一轮 P3+ 优化重起 v2。

## M5 落地清单(2026-05-10 ✅)

- **P0 上线阻塞**:✅ Wave 1 Layout + Wave 2 HomeView/TeacherDetail/Profile + Wave 3 MyOrders 全部落地
- **P1 转化关键**:✅ Wave 4 TeacherList 搜索筛选 + PackageList 对比表 + 共享组件沉淀
- **P2 视觉打磨**:✅ Wave 5 Auth 双栏 + Classroom 100dvh + Review 步骤化 + Legal 锚点
- **后端 endpoint**:✅ booking/counts(Wave 3)+ teacher/list 7 参数 + count(Wave 4)+ package/list currency + free-trial-status(Wave 4)+ review API 改(Wave 5)
- **SQL patch**:✅ 单文件 `20260510_140000_M5_ux_overhaul.sql`(teacher_profile + package + review 各 4 字段 + platform_config 扩 6 tag)
- **i18n**:✅ 4 套 913 leaf keys 完全对齐,ar 11 处 [需运营校对]
- **时间**:plan 估 8.7-10.7 工作日,实际 ~8 小时(subagent 并行)

⏳ **未落地(运维 / 用户参与)**:
- 真机测试矩阵(iPhone SE3 / 15 / iPad mini 6 + iOS ≥ 16.4)
- 后端 SQL patch 上 prod + i18n_message review.tag.* 4 语言种子运维落
- ar 阿语翻译运营校对(M5 阿语市场前)
- /ultrareview 多 reviewer 云审 + 合并 main(用户触发)

---

---

## 0. 为什么有这份文档

PRD-v4 § 4.2 / § 4.3 都明确写"响应式 PC + H5",§ 5 非功能性需求把"双端 = 响应式同源(同一 Vue 工程适配 PC + H5)"列为硬约束。`UI风格规范-v1.md` § 5 / § 6 进一步规定 768px 是 PC/H5 分水岭,`<768` 用 Vant、`≥768` 用 Element Plus。

但代码层面我做了一轮全量 grep + 抽样 Read 后发现:

- 全部 20 个 view 文件**没有一处**使用 Vant 组件(Vant 仅在 `App.vue` 注册了主题色 + RTL)
- `app/src/layouts/` 是**空目录**——没有任何全局 Layout、AppHeader、AppTabBar
- 各页 topbar / 返回 / 语言切换**全靠每个页面自己手写一份 SCSS**,无共享
- 双端处理方式仅是 `@media (min-width: bp-tablet)` 调整字号 / 内边距,**不是真正的 PC vs H5 重排版**
- 首页 HomeView 内容仅 Hero + 两个 CTA(60 行模板),无社交证明 / 老师预览 / 套餐预览 / FAQ / Footer
- 信息架构:登录后 7 个页面("我的订单 / 我的套餐 / 我的退款 / 个人中心 / 套餐购买 / 课堂 / 评价")没有全局菜单聚合,**全靠 URL 直跳**

这是一个"功能跑通的工程态",**不是一个对最终用户拍板的产品态**。M4 Stripe e2e 全绿后,UI 是上线前最大的待补项,影响:

- **转化漏斗**:首页转化路径过短,跳出率高
- **移动端留存**:H5 无底部 Tab Bar,登录后导航回路完全断
- **品牌一致性**:每页自手写 topbar,品牌氛围分散
- **多语言验收成本**:RTL/4 语言要逐页跑,没有共享 Layout 组件兜底

---

## 1. 现状 vs 规范偏离矩阵

### 1.1 框架层

| 项 | 规范/PRD 要求 | 代码现状 | 偏离严重度 |
|---|---|---|---|
| 全局 Layout | 隐含(双端必须有 AppHeader / AppTabBar) | `app/src/layouts/` 空目录 | 🔴 P0 |
| H5 底部 Tab Bar | PRD-v4 § 4.3 学生端 H5 必备 | 不存在 | 🔴 P0 |
| PC 顶部固定导航 | 规范 § 5 PC 视觉默认 | 不存在;各页自写 topbar | 🔴 P0 |
| 路由元信息(meta.title / meta.layout) | 隐含 | 仅 `meta.public` `meta.guestOnly`,无 layout 切换信号 | 🟠 P1 |
| 共享空态组件 `<EmptyState>` | 规范 § 6.2 预期沉淀 | 不存在;各页 `tl-state` `mp-empty` 各写一遍 | 🟠 P1 |
| 共享 `<TeacherCard>` `<PriceTag>` | 规范 § 6.2 预期沉淀 | 不存在 | 🟠 P1 |
| 错误边界 / 全局 Loading | 隐含 | 不存在 | 🟡 P2 |

### 1.2 组件库纪律

| 项 | 规范要求 | 代码现状 | 严重度 |
|---|---|---|---|
| `<768` 视图用 Vant | 规范 § 6 | **0 处 Vant 组件使用**,全部 Element Plus 或 hand-rolled | 🔴 P0 |
| `≥768` 视图用 Element Plus | 规范 § 6 | 部分页用 EP(Login/Register/Profile),部分 hand-rolled(Home/TeacherList/TeacherDetail/MyOrders/Classroom) | 🟠 P1 |
| 同页面不混用 EP+Vant Dialog | 规范 § 6.1 | 不混用(因为 Vant 没用) | ✅ |

**关键解读**:规范 § 6"<768 用 Vant"在落地前必须做出一个**项目级决策**——是按规范全面接 Vant?还是更新规范,改为"全栈 Element Plus,通过 SCSS 在 H5 上单独优化体验"?见 § 4.1 P0-1 的两条路线对比。

### 1.3 视觉一致性

| 项 | 规范/视觉样板要求 | 代码现状 | 严重度 |
|---|---|---|---|
| design-token 使用率 | 100% 来自 `brand.scss` | 抽样 5 个页面,主色 / 圆角 / 字号 token 命中率 ~95%,有零星 `13px` `7px` 偏差 | 🟢 OK |
| 主色硬编码 | ❌ 禁止 | grep 用户端 `*.vue` 无 `#FFB627`、`#FFA60A` 字面量(brand.scss 例外) | ✅ |
| Logo 引用 | `01-logo/` 目录 | HomeView/LoginView 引用 `/logo.svg`,public 目录有 logo,但未引用 deliverables-v1 矢量源 | 🟡 P2 |
| 页面字体阶梯 | 规范 § 2.1 | 命中良好;Hero 用 Georgia 斜体已落地 | ✅ |

### 1.4 双端覆盖度逐页诊断

| 页面 | EP 使用 | hand-rolled | 有 @media | PC 视觉合理 | H5 视觉合理 | 双端总评 |
|---|---|---|---|---|---|---|
| `/` Home | ✗ | ✓ | ✓ | 🟡 单 Hero 太空 | 🟡 单 Hero 还行 | 🔴 内容空,转化弱 |
| `/login` | ✓ | ✗ | ✓ | 🟢 居中卡片 OK | 🟢 OK | 🟡 视觉氛围弱 |
| `/register` | ✓ | ✗ | ✓ | 🟢 OK | 🟡 长表单滚动累 | 🟡 H5 改 Vant cell 体验更好 |
| `/teachers` | ✗ | ✓ | ✓ | 🟡 chip 单维度 | 🟡 单列卡片 OK | 🟠 筛选 / 搜索 / 排序缺 |
| `/teacher/:id` | ✗ | ✓ | ✓ | 🟡 单列长滚动 | 🟢 OK | 🔴 PC 应分左右栏 |
| `/level-check` | ✗ | ✓ | ✓ | 🟢 单题居中 OK | 🟢 OK | 🟢 |
| `/level-check/result/:id` | ✗ | ✓ | ✓ | 🟢 OK | 🟢 OK | 🟢 |
| `/packages` | ✗ | ✓ | ✓ | 🟡 卡片网格 | 🟡 单列 | 🟠 缺套餐对比表 |
| `/orders` | ✗ | ✓ | ✓ | 🟠 1098 行单文件 | 🔴 H5 tab + dialog 全手写 | 🔴 H5 体验差 |
| `/my/packages` | ✗ | ✓ | ✓ | 🟢 OK | 🟢 OK | 🟢 |
| `/my/refunds` | ✗ | ✓ | ✓ | 🟢 OK | 🟢 OK | 🟢 |
| `/profile` | ✓ | ✗ | ✓ | 🟠 760 行混 4 区块 | 🟠 EP form 在 H5 体验一般 | 🔴 信息架构需拆 |
| `/classroom/:id` | ✗ | ✓ | ✗ **无** | 🟢 iframe 全屏 OK | 🔴 100vh + iOS Safari 工具栏穿透 | 🟠 |
| `/review/:id` | ✗ | ✓ | ✓ | 🟡 长表单 | 🟡 长表单 | 🟡 |
| `/booking/success/:id` | ✗ | ✓ | ✓ | 🟢 OK | 🟢 OK | 🟢 |
| `/payment/success` | ✗ | ✓ | ✓ | 🟢 OK | 🟢 OK | 🟢 |
| `/payment/cancel` | ✗ | ✓ | ✓ | 🟢 OK | 🟢 OK | 🟢 |
| `/auth/callback/:p` | ✗ | ✓ | — | — | — | OAuth 中转,N/A |
| `/legal/privacy` | ✗ | ✓ | ✓ | 🟢 长文 OK | 🟢 OK | 🟢 |
| `/legal/terms` | ✗ | ✓ | ✓ | 🟢 长文 OK | 🟢 OK | 🟢 |

**红色行(🔴)= 上线阻塞**:Home / 我的订单(H5)/ 课堂(H5)/ 个人中心 / 教师详情(PC)。这五个页面是 P0 的核心战场。

### 1.5 用户路径断点(信息架构)

```
登录后用户高频路径(对照 PRD-v4 §4.3 学生端 S1-S8):

S1 浏览老师 → S2 老师详情 → S3 选套餐 → S4 预约扣次 → S6 课堂 → S7 评价 → S8 我的套餐
                                          ↓
                                       支付环节(支付成功 → 订单页)

当前用户在登录后想从「我的订单」跳「我的套餐」要怎么走?
  - PC: 浏览器 URL 改 / 个人中心翻链接
  - H5: 同上(无底部 Tab)
  
当前用户在「课堂」上完课想看评价表单要怎么走?
  - 课堂页轮询 order.status,finished 自动跳 review(✅ 已落地)
  - 但是用户中途离开课堂呢?topbar 只有"← 返回订单",没有"我的"
```

**信息架构问题**:

1. 登录态主导航缺位 → 所有"我的 X"页面是孤岛
2. 个人中心(`/profile`)塞了 4 个内容(资料 / 账号绑定 / 教师统计 / 推荐战绩),760 行单文件,无一级菜单分割
3. 公开页 topbar 不一致(HomeView 用 LanguageSwitcher 浮动右上 / TeacherListView 用 `tl-topbar` 写返回链接)

---

## 2. 双端体验问题逐项

按页面 + 视口拆,只列实际有问题的;无问题的略过。

### 2.1 全局框架(影响所有页面)

**P0 阻塞 — 缺全局 Layout**

| 视口 | 现状 | 问题 |
|---|---|---|
| PC | 各页自写 topbar | 视觉零散,登录后无全局导航,品牌氛围弱 |
| H5 | 各页自写 topbar | 无底部 Tab Bar,登录后导航回路断;LanguageSwitcher 位置每页不同 |
| RTL | LanguageSwitcher 右上(LTR) → 左上(RTL),已用 `inset-inline-end` 处理 | OK |

**改造目标**:

- 新增 `app/src/layouts/AppLayout.vue` 作为壳,根据 `route.meta.layout` 决定显示 Header / TabBar
- `AppLayout.vue` 包含:
  - `<AppHeader>` PC ≥768 显示;H5 隐藏(被 page topbar 替代或用纯 logo)
  - `<AppTabBar>` H5 <768 登录态显示;PC 隐藏
  - `<RouterView>` 主内容
- `AppHeader` 包含:Logo + 主导航(老师 / 套餐 / 我的) + 语言切换 + 登录/头像
- `AppTabBar` 包含:首页 / 老师 / 课堂(订单)/ 我的(4 项,Cambly 同款节奏)
- 公开营销页(/、/teachers、/teacher/:id、/level-check、/legal/*)用同一 Layout(显示 AppHeader,登录态决定显示登录按钮 vs 头像)
- 鉴权页(/login、/register、/reset-password)用 AuthLayout(无 Header / 无 TabBar,仅居中卡片 + 角落语言切换)
- 课堂页(/classroom/:id)用 FullscreenLayout(无 Header / 无 TabBar,iframe 占满)

### 2.2 HomeView(`/`)— P0

**现状**:60 行模板,Hero(标题 + tagline + 2 CTA)+ 浮动语言切换。

**问题**:

- 跳出率高:用户点开首页只有"浏览老师 / 等级测试"两个按钮,没看到老师 / 价格 / 课堂样子 / 用户证言就走了
- SEO 内容贫瘠:首页 JSON-LD 已经写了,但页面没有可被 LLM Agent 摘取的内容(`docs/frontend/seo-geo-checklist.md`)
- 缺 footer:法务链接、联系方式、品牌 slogan、备案号(香港主体已下证,详见 `docs/compliance/company-entity.md`)
- 阿语 RTL 模式下大标题用 Georgia 斜体,但阿语字体回退会让标题视觉跳脱

**改造方案**:

```
┌─────────────────────────────────────────────┐
│ AppHeader: Logo · 老师 套餐 等级测试 │ 登录 │  ← PC 顶栏
├─────────────────────────────────────────────┤
│                                             │
│         Mandarly                            │
│   每周一节,口语流利                         │
│   [ 浏览老师 ]  [ 30 秒等级测试 ]            │
│                                             │
│   "为什么选 Mandarly" 三栏 icon 卡          │  ← 新增
│   ┌───┐ ┌───┐ ┌───┐                         │
│   │ 1 │ │ 2 │ │ 3 │                         │
│   └───┘ └───┘ └───┘                         │
│                                             │
├─────────────────────────────────────────────┤
│   推荐老师横向 carousel(取 6 位 top 老师)  │  ← 新增,用 listTeachers API 取
│   [TeacherCard] [TeacherCard] [TeacherCard]  │
├─────────────────────────────────────────────┤
│   套餐预览三列(免费体验 / 半年 / 一年)     │  ← 新增,链 packagesAPI
│   [PackageCard] [PackageCard] [PackageCard]  │
├─────────────────────────────────────────────┤
│   学员评价 carousel(假数据先,M5 接真评价) │  ← 新增,延后真实数据
├─────────────────────────────────────────────┤
│   FAQ 折叠(6 个高频问题)                   │  ← 新增,SEO + GEO 价值高
├─────────────────────────────────────────────┤
│ AppFooter: 关于我们 · 隐私 · 条款 · 备案     │  ← 新增,固定
└─────────────────────────────────────────────┘
```

H5 视图:hero 单列、3 卡纵向 stack、carousel 仍横向滑、套餐 1 列。

**影响文件**:
- `app/src/views/home/HomeView.vue`(重写)
- `app/src/components/home/`(新增 FeatureCard / TeacherCarousel / PackagePreview / TestimonialCarousel / HomeFAQ)
- `app/src/i18n/locales/{en,zh-CN,zh-TW,ar}.js`(新增 home.* key 大约 30 条)

**估时**:1.5 天(含 4 套 i18n + RTL 截图 + MCP 自测)

### 2.3 LoginView / RegisterView(`/login` `/register`)— P2

**现状**:LoginView 居中卡片 420px,el-tabs(邮箱 / 手机 / 第三方),视觉接近上线。RegisterView 长表单,EP form。

**问题**:

- 视觉氛围**工程默认感**——白卡片、灰底,品牌主色仅在 tab 高亮,没有"暖琥珀 + 中文学习"的调性
- H5 上 RegisterView EP form-item label-position="top" + 长滚动,体验一般(EP form 不为窄屏优化)
- 卡片左侧 PC 视图没有品牌氛围图——Cambly / Italki 都做了"半屏 hero 图 + 半屏表单"
- 卡片底部"还没账号 / 去注册"链接对比度偏低
- alert 提示用 `ElMessage`,在 H5 上位置偏上(default top: 20px),阿语 RTL 下方向无问题

**改造方案**:

```
PC ≥768 视图(双栏):
┌──────────────┬──────────────────────┐
│              │                      │
│  hero 插画   │   登录卡片(420px) │
│  + 品牌色    │   tabs / 表单        │
│  + slogan    │                      │
│              │                      │
└──────────────┴──────────────────────┘

H5 <768 视图(单栏):
┌──────────────────────┐
│   logo + slogan      │
│   登录卡片(全宽)   │
│   tabs / 表单        │
└──────────────────────┘
```

**影响文件**:
- `app/src/layouts/AuthLayout.vue`(新增)
- `app/src/views/auth/LoginView.vue` `RegisterView.vue` `ResetPasswordView.vue` `TeacherRegisterView.vue`(套 AuthLayout)
- 视觉稿背景插画从 `docs/brand/deliverables-v1/` 出图(品牌物料范围内)

**估时**:0.5 天(layout)+ 0.5 天(4 个 auth 页适配)

### 2.4 TeacherListView(`/teachers`)— P1

**现状**:`tl-topbar`(返回 + 语言切换)+ hero 标题 + chip 筛选(基于教师 expertise 集合动态生成)+ 卡片网格。

**问题**:

- 筛选维度**单一**:PRD-v4 § S1 要求"教学方向 / 口音 / 价格区间 / 可约时段 / 评分",目前只有 expertise 一个维度
- **无搜索框**:Cambly 一进列表页就有搜索 orb,Mandarly 没有
- **无排序切换**:PRD 要求"推荐 / 好评 / 价格 / 新教师"排序,目前默认顺序
- PC 视图卡片是 `flex-wrap`,2-3 列;但**列宽不固定**,4-5 列也可能,布局飘
- H5 视图单列,卡片信息密度足够,OK
- 教师可约时段(`available_slot_count`)在卡片上没有展示,影响"立即可约"决策

**改造方案**:

- 顶部加搜索 orb(Airbnb 同款,半径 32px pill,主色 focus ring)
- 筛选 + 排序合并成"过滤抽屉"(H5)/ 顶部条(PC)
  - 过滤项:教学方向、口音、价格区间(slider)、可约时段(今天 / 本周 / 不限)、评分(≥4.5 / ≥4.0)
  - 排序项:推荐(默认)、好评 ↓、价格 ↑、价格 ↓、新教师
- 教师卡显示"今日可约 X 时段"或"最早 X 月 X 日可约"标签(主色徽章)
- PC 视图固定 3 列(`grid-template-columns: repeat(3, 1fr)`),≥1280 切 4 列

**影响文件**:
- `app/src/views/teacher/TeacherListView.vue`(改)
- `app/src/components/TeacherCard.vue`(新增,共享给 Home / TeacherList)
- `app/src/components/teacher/TeacherFilterDrawer.vue`(新增)
- `app/src/api/teacher.js`(后端筛选 / 排序参数对接)
- 后端 `mandarly-module-edu/teacher/` 加 listTeachers 排序 / 筛选参数

**估时**:1 天(前端)+ 0.5 天(后端 API 增强)

### 2.5 TeacherDetailView(`/teacher/:userId`)— P0

**现状**:1128 行单文件,hand-rolled,无 EP。包含:头像、介绍、专长 chip、评价列表、日历选时段。

**问题**:

- **PC 视图单列长滚动**——头像 / 介绍 / 评价 / 日历堆一柱;1280 屏宽利用率仅 50%
- 行业惯例(Cambly / Italki)PC 是**左侧介绍 / 右侧 sticky 预约面板**,Mandarly 现在没做
- H5 视图单列 OK,但"立即预约 CTA"应固定吸底(sticky bottom),目前是滚到日历才能选时段
- 评价列表分页 / 加载方式没看到,可能是一次性渲染
- 教师介绍可能有英文长文 + 中文长文,RTL 阿语切换时 bidi 边界要再校验

**改造方案**:

```
PC ≥1024 视图:
┌────────────────────────────┬────────────────┐
│  教师介绍 + 评价列表        │  预约面板      │
│  (滚动)                   │  sticky top    │
│  - 头像 + 名字 + 标签       │  - 日历        │
│  - 评分 + 课时数            │  - 时段        │
│  - 自我介绍                 │  - [ 立即预约 ]│
│  - 教学专长                 │                │
│  - 评价分页                 │                │
└────────────────────────────┴────────────────┘

PC <1024 视图:介绍占满 → 评价占满 → 预约面板占满(单列)

H5 <768 视图:
┌────────────────────────┐
│  头像 + 名字            │
│  评分 / 标签 / 介绍     │
│  评价列表                │
│  日历选时段              │
│                         │
├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
│  [ 立即预约 ] sticky    │  ← H5 吸底 CTA
└────────────────────────┘
```

**影响文件**:
- `app/src/views/teacher/TeacherDetailView.vue`(分两栏 + sticky)
- 评价列表抽 `<ReviewList>` 共享组件,加分页
- 日历组件 `<ScheduleSlotPicker>`(已存在)需在 sticky 容器里能滚动

**估时**:1.5 天

### 2.6 PackageList / 套餐购买流程(`/packages`)— P1

**现状**:卡片列表,套餐名 / 课次 / 有效期 / 价格 / 立即购买。

**问题**:

- 用户决策成本高——三档套餐(半年 1次/周 / 半年 2次/周 / 一年 1次/周 / 一年 2次/周 / 单次试课)**没有对比表**
- "免费体验课"和付费套餐**视觉同列**,容易让新用户误以为也要付钱(应该用 banner 突出)
- "一年包 9 折"折扣信息没有视觉标记(应加"省 X HKD"徽章)
- 套餐选择 → Stripe checkout 跳转中没有"返回"路径,失败需重选
- 多币种(HKD / USD / CNY)切换:目前根据 user.locale 自动选,但用户可能想切币种再决策

**改造方案**:

- PC 视图加套餐对比表(横向 5 列,行:课次 / 时长 / 价格 / 单价 / 推荐人群)
- "免费体验课"独立 banner 置顶(主色 soft 底)
- 一年包加"省 X HKD"主色徽章,放卡片右上
- 卡片加"推荐"标签(给"半年包 1次/周"——一期主推)
- 币种切换器加在套餐卡上方(若 user 已设 locale 自动选,但允许 override)

**影响文件**:
- `app/src/views/package/PackageListView.vue`
- `app/src/components/package/PackageCompareTable.vue`(新增,PC only)
- `app/src/components/PriceTag.vue`(新增,共享多币种格式化)

**估时**:1 天

### 2.7 MyOrdersView(`/orders`)— P0

**现状**:1098 行单文件,hand-rolled tab(`all / upcoming / toReview / finished / cancelled / refunding`)+ 卡片列表 + 取消弹窗(hand-rolled overlay)。

**问题**:

- **1098 行单文件**——tab / 列表 / 卡片 / 取消弹窗 / 24h 边界提醒全塞一起,可读性差
- H5 视图 tab 横向滚动 OK,但 6 个 tab 在窄屏需要左右滑(无 indicator)
- 取消弹窗 hand-rolled,**没用 Vant Popup**,在 H5 上动效粗糙
- 卡片信息密度 PC 偏稀(一行只有几个字段),H5 单列 OK
- "24h 边界缓冲"只在弹窗里提示,卡片上没有"距上课还有 X 小时"倒计时
- 退款 tab(M4+ 才有真实数据)目前是空态 + "敬请期待",过渡到 M4 上线时是否替换有待校对

**改造方案**:

- 拆三个文件:`MyOrdersView.vue`(壳 + tab)/ `OrderCard.vue`(卡片)/ `OrderCancelDialog.vue`(取消弹窗)
- 卡片加"距上课 X 小时 X 分"实时倒计时(dayjs duration)
- H5 tab 改用 Vant Tabs(`<van-tabs sticky>`)或继承 EP `el-tabs` 但加 sticky + indicator
- 取消弹窗:H5 改用 Vant Popup 或 Action Sheet(底部弹起,符合移动端习惯);PC 仍用 EP Dialog
- "可加入课堂"按钮在课前 30 分钟才可点(目前是 isClassJoinable 已实现,但视觉禁用态可加倒计时)

**影响文件**:
- `app/src/views/booking/MyOrdersView.vue`(拆分)
- `app/src/components/booking/OrderCard.vue`(新)
- `app/src/components/booking/OrderCancelDialog.vue`(新)

**估时**:1.5 天

### 2.8 ClassroomView(`/classroom/:orderId`)— P1

**现状**:160 行,顶部 topbar(返回 + 标题)+ iframe(LCIC),height: 100vh。

**问题**:

- **iOS Safari 100vh 穿透 bug**——浏览器底部工具栏会遮挡 iframe 底部 80px,LCIC 控制按钮可能被挡
- 无任何 @media 响应式调整(PC + H5 视觉一致,但 H5 上 topbar 占比偏大)
- 离开课堂的提示弱——topbar"← 返回"没有 confirm,误点会断课
- 网络断开 / iframe 加载失败的提示是 errorMsg 文本,无重连按钮
- 课程结束 5s 轮询 → 跳评价页;轮询失败容忍但没有 fallback timer 兜底(M3+ 上线时若 webhook 漏触发,前端轮询撑住)

**改造方案**:

- height 改 `100dvh`(dynamic viewport,Safari 16.4+ 支持;Chrome 全支持)+ fallback `100vh`
- 离开课堂改 `confirm`(EP MessageBox / Vant Dialog),阿语 RTL 文案过校
- iframe 加 `onerror` + 重连按钮
- topbar 在 H5 视图缩小高度(48px → 40px)

**影响文件**:
- `app/src/views/classroom/ClassroomView.vue`

**估时**:0.5 天

### 2.9 ProfileView(`/profile`)— P0

**现状**:760 行单文件,EP form(资料)+ EP 卡片(账号绑定)+ 教师统计(条件渲染)+ 推荐战绩 + 退出按钮 + 邮箱 / 手机绑定 dialog。

**问题**:

- **信息架构混乱**——4 大块塞一页,垂直滚动长,用户找"账号绑定"要拖很久
- 教师角色 vs 学生角色看到的内容不同(`isTeacher` 条件渲染),但**入口都是 `/profile`**,体验割裂
- H5 上 EP form-item label-position="top" + size="large" 占垂直空间大
- 推荐战绩展示用了几个数字,但**没图表**,转化感弱
- 退出登录按钮放最底,危险操作应该有二次确认
- 时区 select 选项 hardcode 7 个,加纳市场上线后(`Africa/Accra`)需补,目前 hardcode 列表会成 PR 障碍

**改造方案**:

- 信息架构拆成 3 个一级菜单(顶部 tab 或左侧菜单):
  - `/profile/account` 账户(资料 + 账号绑定)
  - `/profile/referral` 推荐战绩(独立页,加图表)
  - `/profile/teacher-stats` 教学统计(仅教师可见)
- 主 ProfileView 仅作壳 + 路由分发
- 时区 select 改用 Intl API 动态生成(`Intl.supportedValuesOf('timeZone')` 浏览器原生),按 region 分组
- 退出登录加 Confirm,文案"确认要退出当前账号吗?未支付订单不会受影响"
- H5 视图 form 改用 Vant `<van-cell-group>` + `<van-field>`(label inline / 触控区大)

**影响文件**:
- `app/src/views/profile/ProfileView.vue`(壳 + 路由分发)
- `app/src/views/profile/AccountView.vue` `ReferralView.vue` `TeacherStatsView.vue`(新增,从 ProfileView 拆)
- `app/src/router/index.js`(子路由 children)

**估时**:1.5 天

### 2.10 ReviewFormView(`/review/:orderId`)— P2

**现状**:473 行,长表单,评分 + 标签 + 文字。

**问题**:

- 表单偏长,H5 上滚动疲劳
- "评价"是高情感任务,视觉应该轻松愉悦,目前比较工程化
- 五星评分组件(假定 hand-rolled)在 RTL 下方向需校验

**改造方案**:

- 拆分步骤:Step 1 给老师评分 → Step 2 给课程标签(快选) → Step 3 写文字(可选)
- 主色徽章 + 五星动效

**估时**:0.5 天(P2 时再做)

### 2.11 RefundListView(`/my/refunds`)— P3

P3 不展开,M4 e2e 已通过,UI 可后续打磨。

### 2.12 LegalPrivacy / LegalTerms — P2

新加(GCP OAuth consent screen 必填),内容是长文 + 锚点。建议:

- 加左侧 sticky 锚点导航(PC)/ 顶部锚点跳转(H5)
- 长文加阅读进度条
- 字号 / 行高 / 段距用规范 token

---

## 3. 优化路线 P0 → P3

### 3.1 P0 上线阻塞(必做,预计 6-8 工作日)

| # | 任务 | 关键文件 | 估时 | 依赖 |
|---|---|---|---|---|
| P0-1 | **决策:Vant 是否真的用** | `UI风格规范-v1.md` § 6 重审 | 0.5 天 | 见 § 4.1 决策树 |
| P0-2 | 全局 Layout 体系(AppLayout / AuthLayout / FullscreenLayout) | `app/src/layouts/*` 新增 | 1 天 | P0-1 |
| P0-3 | AppHeader(PC ≥768) | `app/src/layouts/AppHeader.vue` | 0.5 天 | P0-2 |
| P0-4 | AppTabBar(H5 <768 登录态) | `app/src/layouts/AppTabBar.vue` | 0.5 天 | P0-2 |
| P0-5 | AppFooter(法务 + 备案号 + 联系) | `app/src/layouts/AppFooter.vue` | 0.5 天 | P0-2 |
| P0-6 | HomeView 改造(hero + 老师预览 + 套餐预览 + FAQ + footer) | `app/src/views/home/*` | 1.5 天 | P0-2,P0-3 |
| P0-7 | ProfileView 信息架构拆分(account / referral / teacher-stats) | `app/src/views/profile/*` 拆 | 1.5 天 | P0-2 |
| P0-8 | TeacherDetailView PC 双栏 + H5 sticky 预约 | `app/src/views/teacher/TeacherDetailView.vue` | 1.5 天 | — |
| P0-9 | MyOrdersView 拆分 + H5 取消弹窗优化 | `app/src/views/booking/*` 拆 | 1.5 天 | P0-1 |
| P0-10 | i18n 4 套同步增删(P0 全部新文案) | `app/src/i18n/locales/*` | 0.5 天 | P0-6 ~ P0-9 |
| P0-11 | MCP Playwright PC + H5 + RTL 三套截图回归(全部 P0 改的页) | `.playwright-mcp/` | 0.5 天 | 所有 P0 |

**P0 总估时**:9 工作日(单人)/ 5-6 工作日(2-3 subagent 并行写关键页)

### 3.2 P1 转化关键(2-3 工作日)

| # | 任务 | 估时 |
|---|---|---|
| P1-1 | TeacherListView 加搜索 + 多维度筛选 + 排序 | 1 天 |
| P1-2 | TeacherCard 共享组件(Home / TeacherList 共用) | 0.5 天 |
| P1-3 | PackageListView 加对比表 + 推荐徽章 + 币种切换 | 1 天 |
| P1-4 | EmptyState / LoadingState / ErrorState 共享组件 | 0.5 天 |
| P1-5 | route meta.title + 全局 document.title 联动(SEO) | 0.5 天 |

### 3.3 P2 打磨(2-3 工作日,可与 M5 合并)

| # | 任务 | 估时 |
|---|---|---|
| P2-1 | LoginView / RegisterView PC 双栏 + 品牌氛围图 | 1 天 |
| P2-2 | ClassroomView 100dvh + 离开 confirm + iframe 错误重连 | 0.5 天 |
| P2-3 | ReviewFormView 步骤化 + 视觉轻松 | 0.5 天 |
| P2-4 | LegalPrivacy / LegalTerms 锚点 + 阅读进度 | 0.5 天 |
| P2-5 | 全站 Logo 引用统一到 `docs/brand/deliverables-v1/01-logo/` | 0.5 天 |

### 3.4 P3 可选(上线后迭代)

| # | 任务 |
|---|---|
| P3-1 | 暗色模式(brand.scss 加 dark token,system prefers-color-scheme) |
| P3-2 | PWA(manifest + service worker + 离线提示) |
| P3-3 | 性能:首页骨架屏 / 路由级懒加载校验 / 图片懒加载 |
| P3-4 | A/B 测试基础设施(首页 hero CTA / 套餐对比表样式) |
| P3-5 | 教师注册流程优化(目前 TeacherRegisterView 是 12KB 长表单) |

---

## 4. 关键决策点(P0 启动前必须拍板)

> **本节三个决策已于 2026-05-09 由 OPC 拍板定版**,后续会话直接按此执行,无需再讨论。
> - 决策 1: ✅ **路线 A(全栈 Element Plus,Vant 仅作补充)**
> - 决策 2: ✅ **H5 底部 Tab Bar 4 项**(首页 / 老师 / 我的课 / 我的)
> - 决策 3: ✅ **AppHeader 在 H5 显示精简版**(logo + 语言切换,登录后头像换 logo 旁;详情 / 列表深层页用各页 topbar 替代)

### 4.1 决策 1:Vant 要不要真的用?

这是 P0-1,影响后续所有 H5 视觉决策。

**路线 A:全栈 Element Plus(放弃 Vant)**

- 优点:统一一套组件库,认知 / 维护成本低;现状代码已经全 EP / hand-rolled,改动小
- 缺点:H5 上 EP Dialog / Form / DatePicker 体验不如 Vant 自然;移动端典型场景(全屏 popup / action sheet / 时间滚轮)需自封装
- 落地:**修订 `UI风格规范-v1.md` § 6**,把"<768 用 Vant"改为"<768 视图通过 SCSS 优化 EP,必要时局部用 Vant",维护 Vant 仅作为补充而非主选

**路线 B:严格按规范分端(EP + Vant)**

- 优点:严格执行规范,H5 体验更原生
- 缺点:代码层面需要为同一个 dialog / form 写两套(PC EP / H5 Vant),维护成本翻倍;v-if 视口切换组件树容易出 bug;Vant 与 EP 主题色已分别接好但样式细节(z-index / 圆角 / 间距)需对齐
- 落地:在每个高交互页(MyOrders 取消 / Profile 绑定 / Package 购买)写两套实现,通过 useMediaQuery 切换

**OPC 拍板(2026-05-09)**:✅ **执行路线 A**——理由:

1. 现有代码 0 处用 Vant,切到 A 改动小;切到 B 等于推倒重来一次
2. M4 e2e 已通过,上线压力大,B 路线工期不可控
3. Mandarly 当前定位是"PC 优先 + H5 适配"(PRD-v4 § 5 也仅说"双端 = 同源",未强制 H5 体验对标原生 App),A 路线足够生产级
4. 后续(M5+)某些纯 H5 高频页(如教师 H5 排课设置)可局部引入 Vant,不强制全局

**已落地动作**:`UI风格规范-v1.md` § 6 已同步修订为路线 A(2026-05-09)。

### 4.2 决策 2:H5 底部 Tab Bar 4 项还是 5 项?

| 方案 | 底栏 Tab | 优 | 劣 |
|---|---|---|---|
| 4 项 | 首页 / 老师 / 我的课 / 我的 | 极简,符合 Cambly 节奏 | "套餐购买"无入口,需在"我的"二级 |
| 5 项 | 首页 / 老师 / 套餐 / 我的课 / 我的 | 套餐曝光,商业转化好 | 5 项偏挤 |

**OPC 拍板(2026-05-09)**:✅ **4 项**,套餐入口放首页 + 教师详情;"我的"页里有"我的套餐"。理由:5 项底栏在 320px 窄屏下文字会挤。

### 4.3 决策 3:AppHeader 在 H5 上如何处理?

| 方案 | 现状 | 优劣 |
|---|---|---|
| H5 隐藏 AppHeader,各页自带 topbar | 接近现状 | 各页 topbar 不一致 |
| H5 显示精简版 AppHeader(仅 logo + 语言切换) | 新方案 | 一致性好 |
| H5 上仍显示完整 AppHeader,但配 hamburger | 复杂 | 移动端用户对 hamburger 习惯下降 |

**OPC 拍板(2026-05-09)**:✅ **方案 2**——H5 上显示 logo + 语言切换的精简 header(高度 48px),登录后头像换 logo 旁;详情页 / 列表页等深层页面仍用各页"返回 + 标题"的 topbar 替代 AppHeader。

---

## 5. 验收 Checklist(双端补充)

继承 `UI风格规范-v1.md` § 10 的 12 项,P0 / P1 改造后每页额外加:

```
[ ] 13. PC ≥1280 视图:布局合理(无大片留白 / 无横向滚动)
[ ] 14. PC 1024-1279 视图:同上
[ ] 15. PC 768-1023 视图:同上(过渡期视觉,常被忽略)
[ ] 16. H5 375 视图(iPhone SE / 13 mini):无横向滚动 / 触控区 ≥ 44px
[ ] 17. H5 320 视图(iPhone 5/SE 老款):核心 CTA 不溢出
[ ] 18. H5 414 视图(iPhone Pro Max):布局充分利用
[ ] 19. iOS Safari 100vh / 100dvh:全屏页面底部不被工具栏遮挡
[ ] 20. 登录态在 H5 底部 Tab Bar 当前项高亮正确
[ ] 21. 鉴权页(login / register / reset)无 Header / 无 TabBar
[ ] 22. 课堂页(classroom)全屏,无 Header / 无 TabBar
[ ] 23. RTL 视图下 Tab Bar 顺序镜像 / Header 内 logo 位置正确
[ ] 24. 路由切换时 document.title 自动更新(SEO)
```

MCP 自测时三套截图(PC 1280 / H5 375 / RTL 1280)是基础,P0 改造的页要额外跑:
- PC 1024(平板横屏)
- H5 414(大屏手机)
- iOS Safari 模拟器(100vh 穿透校验)

---

## 6. 文档关系图

```
docs/frontend/
├── README.md                     ← 索引,本文加进去
├── UI风格规范-v1.md              ← 工程契约(token / 组件库 / RTL / i18n / Checklist)
│                                  末尾 § 11 加 reference 指向本文
├── UX审计与优化-v1.md  ★ 本文     ← 审计 + P0-P3 路线图
├── SEO-GEO-清单.md               ← 公开页面 SEO + LLM 可见性(Home / TeacherList 改造时同步对照)
└── 视觉参考/
    ├── README.md
    ├── DESIGN-mandarly-v1.md     ← 视觉样板(组件级,Mandarly 适配版,主用)
    └── DESIGN-airbnb-原版.md     ← 范式镜像,只读
```

**对接关系**:

- 本文识别问题 → 对照 `UI风格规范-v1.md` 校 token + 校组件库纪律
- 本文每个改造方案 → 落实时按 `视觉参考/DESIGN-mandarly-v1.md` 取组件具体长什么样
- HomeView / TeacherList 改造 → 同步过 `SEO-GEO-清单.md` 校对 meta / JSON-LD / 链接
- 任何 token 变化(新增 design-token)→ 改 `brand.scss` + 同步改 `UI风格规范-v1.md` § 4

---

## 7. 落地节奏建议

按 OPC 工作流 + 已有沉淀的 subagent 模式:

1. **Day 1**:本文落地后,你拍板 § 4 三个决策 → 我把决策 1 修订写入 `UI风格规范-v1.md` § 6
2. **Day 2-3**:P0-2 ~ P0-5 全局 Layout 体系(AppLayout / AppHeader / AppTabBar / AppFooter / AuthLayout / FullscreenLayout)+ MCP 三套截图回归
3. **Day 4-5**:P0-6 HomeView 改造(可起 subagent 并行写 home/* 子组件,主 agent 聚合 i18n)
4. **Day 6-7**:P0-7 / P0-8 / P0-9 可起 3 subagent 并行(profile 拆 / teacher 双栏 / orders 拆),主 agent 兜 i18n + 截图
5. **Day 8**:P0-10 i18n 同步 + P0-11 全量 MCP 回归
6. **Day 9+**:P1 / P2 按需排期

**风险**:
- 阿语 RTL + 4 套 i18n 是隐性工时,P0-10 单日打不完时往后延
- LCIC 课堂页 100dvh 改造需要在真机 iOS Safari 验证,MCP 模拟器结果不完全可信

---

## 8. 变更日志

- **v1 · 2026-05-09**:首版。
  - § 1 现状偏离矩阵(框架层 / 组件库 / 视觉一致性 / 双端覆盖度逐页 / 信息架构)
  - § 2 双端体验问题逐项(12 个页面,只列有问题的)
  - § 3 P0-P3 路线图(P0 9 工作日,P1 3 天,P2 3 天,P3 上线后迭代)
  - § 4 三个关键决策(Vant 路线 / TabBar 项数 / H5 AppHeader 处理)
  - § 5 双端验收 Checklist 补充 12 项
  - § 6 文档关系图
  - § 7 落地节奏建议

---

> **下一步**:你确认 § 4 三个决策后,我开 brainstorming 把 P0-2 ~ P0-5 全局 Layout 设计稿过一遍,过完直接进 plan + 执行。

**项目根**: `/Users/liuguanye/home/Develop/project/orders/mandarly`
**本文**: `/Users/liuguanye/home/Develop/project/orders/mandarly/docs/frontend/ux-audit-v1.md`
