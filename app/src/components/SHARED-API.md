# Mandarly 共享组件 API 契约

> **单一真实源**。Wave 2 / 3 / 4 各 subagent 实现共享组件、引用共享组件时,严格按本文件落 props / slots / events / variant / token。
>
> 修改本文件需在 PR 描述中说明 "**破坏性变更**",并在 commit message 提及 `BREAKING SHARED-API.md` 关键字 + 通知所有引用方(grep `<TeacherCard|<PackageCard|<PriceTag|<EmptyState|<StatCard` 找下游)。
>
> 上游来源:
> - `docs/frontend/visual-reference/DESIGN-mandarly-v1.md` § 共享组件清单(第 8 轮设计稿定版,2026-05-09)
> - `docs/frontend/ui-style-guide-v1.md`(token / RTL / i18n / 时区契约)
> - `app/src/assets/styles/brand.scss`(SCSS / CSS var token 一一对应)
>
> 本文件覆盖 5 个共享组件:`<TeacherCard>` / `<PackageCard>` / `<PriceTag>` / `<EmptyState>` / `<StatCard>`。

---

## 0. 总则(全组件适用)

### 0.1 命名

| 项 | 规则 |
|---|---|
| 组件名 | `PascalCase`(`TeacherCard.vue` / `PackageCard.vue`),无 `Md-` / `Mandarly-` 前缀;EP / Vant / vben 各有自己前缀,直接名字不冲突 |
| 文件位置 | 通用共享: `app/src/components/<Name>.vue`;profile 子域专用: `app/src/components/profile/<Name>.vue` |
| 自动注册 | 已配 `unplugin-vue-components`,放在 `app/src/components/` 下的组件**自动按需引入**,**调用方禁止手动 `import`** |
| props 命名 | `camelCase`;布尔类型用 `is*` / `has*` 前缀(例外:`compact` / `loading` / `free` 这类语义化形容词允许直接用,与 EP / Vant 习惯对齐) |
| 布尔默认 | 一律 `false`(避免使用方拼"反义词" prop) |
| variant 命名 | `string union`(用 `validator` 校验),不用 enum;variant 取值用 `kebab-case`(`list-major` / `list-secondary`) |

### 0.2 事件

- 事件名用 `kebab-case`,例:`@click-favorite` / `@click-buy` / `@click-claim-trial`
- 卡片整体跳转用 `<router-link>`(SEO + 右键打开新 tab + 中键打开),**不要**用 `@click + router.push`
- 内部"次要按钮"(♥ 收藏 / 立即购买 / 立即领取)用事件外抛,不在组件内部决定路由

### 0.3 i18n(关键约束)

**组件内部不直接调 `t()`**。理由:同一组件 4 处复用就 4 套 key 重复,且不同页面的措辞需要细调(Home "查看全部" vs List "更多筛选")。

**调用方负责**:
- 把 `t('xxx.title')` 渲染成字符串后再传入组件 props(如 `title` / `actionText` / `label`)
- 组件 props 里所有"要显示给用户看的字符串"在下文表格里都标注 **i18n: 调用方** 字样

**组件内部允许**直接用 `t()` 的场景(白名单):
- `<PriceTag>` 的"免费"翻译(`zh-CN: 免费 / en: Free / ar: مجانًا`)— 因为这是金额 = 0 的强制短语,各页面措辞一致
- `<TeacherCard>` 的徽章文案(`今日可约` / `新教师`)— 第 8 轮 DESIGN 已固定 i18n key 为 `teacherCard.badge.todayAvailable` / `teacherCard.badge.newTeacher`,跨页一致

### 0.4 RTL(阿语镜像)

- **全部用 logical properties**:`inset-block-start` / `inset-inline-start` / `inset-inline-end` / `padding-inline-*` / `margin-inline-*` / `border-inline-start` / `text-align: end`
- **禁止使用** `left` / `right` / `padding-left` / `margin-right` / `text-align: left|right` 之类物理方向
- bidi 文本(教师介绍可能中英混排)用 `dir="auto"` 让浏览器自动判向

### 0.5 token only(零硬编色 / 字号 / 间距)

- CSS 全部从 `app/src/assets/styles/brand.scss` 取:
  - SCSS 变量:`brand.$brand-primary` / `brand.$radius-card` / `brand.$spacing-3` / `brand.$shadow-md` ...
  - CSS variables(可在 `:style` 内联 / `var()` 用):`var(--color-primary)` / `var(--color-bg-card)` / `var(--radius-card)` ...
- **禁止**写 `#AC722C` / `14px` / `0 4px 16px rgba(...)` 等字面量
- 唯一例外:`<EmptyState>` `--error` variant 的 8px 红晕用 `rgba(255, 77, 79, 0.08)` 字面量(brand.scss 暂未暴露 `--color-error-soft`,M5 后补 token 时同步替换)

### 0.6 a11y

- 卡片整体可点击 → 用 `<router-link>` 自带 a11y;副按钮 ♥ 等用 `<el-button>` + `aria-label`
- 空态 / 错误态 → `role="status" aria-live="polite"`(EmptyState 实现已就位)
- 评分 ⭐ → `aria-label="4.9 stars"`(由调用方传 `aria-label` 字符串)

---

## 1. `<TeacherCard>`

### 1.1 用途

教师信息卡片,跨 Home / TeacherList / TeacherDetail 复用同一组件,通过 `variant` 控制头像比例 / 字段密度 / 浮签显示。

### 1.2 variant

| 取值 | 用在哪些页面 | 用途 |
|---|---|---|
| `home` | `app/src/views/home/HomeView.vue` § 推荐老师 carousel(280px 卡 ×N 横滑) | 首页精华版,头像 4:3 + 名字/评分/价格/单 chip + 浮签 |
| `list` | `app/src/views/teacher/TeacherListView.vue` § E 主网格(2/3/4 列响应式) | 完整版,头像 4:3 + 5 核心字段 + 2 chip + 价格 + 浮签 |
| `compact` | `app/src/views/teacher/TeacherDetailView.vue` "相关老师"(P1+ 占位)+ 搜索建议下拉(P2+) | 简版,头像 1:1 圆 80px + 名字/评分 + 无价无 chip 无浮签 |

> 注:DESIGN.md 第 8 轮 § 8.1 与第 6 轮 § G 已对齐,均为 `home` / `list` / `compact` 三档,无 `detail-similar` / 其它别名。

### 1.3 Props

| 名 | 类型 | 默认 | 必填 | 说明 |
|---|---|---|---|---|
| `teacher` | `Object` | — | ✓ | 教师对象;shape 见 1.3.1 |
| `variant` | `String` | `'list'` | — | 见 1.2,validator 校验 `['home', 'list', 'compact']` |
| `showFavorite` | `Boolean` | `false` | — | 是否显示 ♥ 收藏按钮(P1 占位;一期 disabled + tooltip "敬请期待") |

#### 1.3.1 `teacher` shape

| 字段 | 类型 | 来源 | 备注 |
|---|---|---|---|
| `userId` | `Number` / `String` | 后端 | 跳转用 `/teacher/:userId` |
| `nickname` | `String` | 后端 | 教师昵称(用户生成内容,不翻译) |
| `avatar` | `String` | 后端 | URL,留空时由组件内部用主色 ghost 占位 |
| `accent[]` | `String[]` | 后端(i18n_message code) | 例 `['mandarin_cn', 'cantonese']`;**调用方负责**用 `t()` 解析为本地化文字后通过 `displayAccent: 'String'` 字段重新塞回 teacher 对象,或扁平化为单字段;两种方式必须二选一,本文档推荐前者 |
| `displayAccent` | `String` | 调用方组装 | accent[] 已 join `· ` 后的本地化字串(推荐做法,组件不调 t) |
| `expertise[]` | `String[]` | 后端(i18n_message code) | 例 `['business', 'hsk']` |
| `displayExpertise[]` | `String[]` | 调用方组装 | expertise[] 已本地化的字串数组,前 N 项渲染 chip(home: 1, list: 2, compact: 0) |
| `avgRating` | `Number` | 后端 | 例 `4.9`;组件渲染 ⭐ + 数字 |
| `reviewCount` | `Number` | 后端 | 仅 `list` variant 显示"(120 条)"(可选,看 review 设计) |
| `badges[]` | `String[]` | 后端 | 例 `['todayAvailable']` / `['newTeacher']`,优先级 `todayAvailable > newTeacher`,同时只渲染一个 |
| `todayAvailableCount` | `Number` | 后端 | 备用,组件不直接显示(由 `badges[]` 推导) |
| `auditedAt` | `ISO 8601 string` | 后端 | 备用,组件不直接显示(由 `badges[]` 推导) |

> 调用方有责任把 `accent[]` / `expertise[]` 用 `t()` 渲染为本地化文字塞进 `displayAccent` / `displayExpertise[]`,**TeacherCard 不调 `t()` 解析 i18n_message code**(避免组件依赖 i18n store)。徽章文案是固定 key 例外(见 § 0.3)。

### 1.4 Slots

无对外暴露 slot(P0)。如需扩展(例:list variant 在卡底加"立即预约"按钮),通过新增 prop 控制,不开 slot。

### 1.5 Events

| 名 | payload | 触发场景 |
|---|---|---|
| `click-favorite` | `{ userId }` | ♥ 按钮点击;`@click.stop` 阻止冒泡,卡片整体跳转不触发 |

### 1.6 视觉 token 映射

| 视觉项 | token |
|---|---|
| 卡片底色 | `var(--color-bg-card)` / `brand.$color-bg-card`(白) |
| 圆角 | `brand.$radius-card`(14px) |
| 边框 | hairline 1px `brand.$color-border`(home/list 无边框,compact 可加) |
| hover 阴影 | `brand.$shadow-md` + `transform: translateY(-2px)` + `transition: 0.18s` |
| 主色徽章"今日可约" | `background: brand.$brand-primary` + `color: brand.$color-text-inverse` + `border-radius: brand.$radius-pill` |
| ink 徽章"新教师" | `background: brand.$color-text-primary` + `color: brand.$color-text-inverse` + `border-radius: brand.$radius-pill` |
| 头像 4:3(home/list) | `aspect-ratio: 4 / 3; object-fit: cover` |
| 头像 1:1 圆(compact) | `width: 80px; aspect-ratio: 1 / 1; border-radius: brand.$radius-full` |
| 名字字号 | `brand.$font-size-md`(16px) + `font-weight: 600` |
| 评分字号 | `brand.$font-size-base`(14px) |
| accent 副文案 | `brand.$font-size-base`(14px) + `color: brand.$color-text-tertiary` |
| 价格 | 由 `<PriceTag size="default">` 接管,inline-end |

### 1.7 RTL 注意

- 浮签"今日可约": `inset-block-start: brand.$spacing-3; inset-inline-start: brand.$spacing-3`(LTR 左上 / RTL 右上)
- ♥ 收藏: `inset-block-start: brand.$spacing-3; inset-inline-end: brand.$spacing-3`(LTR 右上 / RTL 左上)
- 价格区域: `text-align: end`(LTR 右 / RTL 左);数字本身保持 LTR(浏览器 bidi 自动)
- chip 行: `flex-wrap: wrap`,gap `brand.$spacing-2`,无 `text-align`

### 1.8 调用示例

```vue
<!-- HomeView.vue 推荐老师 carousel -->
<template>
  <swiper>
    <swiper-slide v-for="t in recommendedTeachers" :key="t.userId">
      <TeacherCard :teacher="t" variant="home" />
    </swiper-slide>
  </swiper>
</template>

<!-- TeacherListView.vue 主网格 -->
<template>
  <div class="teacher-list__grid">
    <TeacherCard
      v-for="t in teachers"
      :key="t.userId"
      :teacher="t"
      variant="list"
      :show-favorite="userStore.isLoggedIn"
      @click-favorite="handleFavorite"
    />
  </div>
</template>

<!-- compact(P1+ 相关老师) -->
<template>
  <TeacherCard :teacher="similarTeacher" variant="compact" />
</template>
```

### 1.9 调用方组装 teacher 对象示例

```js
// useTeacherList.js
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

function decorateTeacher(raw) {
  return {
    ...raw,
    displayAccent: raw.accent.map(code => t(`teacher.accent.${code}`)).join(' · '),
    displayExpertise: raw.expertise.map(code => t(`teacher.expertise.${code}`)),
    period: t('teacherCard.priceUnit'), // 例 '/ 25 分钟'
  }
}
```

---

## 2. `<PackageCard>`

### 2.1 用途

套餐(课程包)信息卡片,跨 Home / PackageList 复用,通过 `variant` 切换密度。

### 2.2 variant

| 取值 | 用在哪些页面 | 用途 |
|---|---|---|
| `home` | `app/src/views/home/HomeView.vue` § 4 套餐预览(3 张精华) | 不显示 sellingPoints,显示推荐 pill / 折扣徽章 / CTA |
| `list-major` | `app/src/views/package/PackageListView.vue` § C 主推 4 卡 | 完整版:推荐 pill + 折扣徽章 + 3 行 sellingPoints + CTA |
| `list-secondary` | 对比表行内 mini 卡片(P2 占位,P0/P1 不实现) | 紧凑版:无推荐 pill / 无折扣徽章 / 无 sellingPoints,仅价格 + CTA |

> 注:DESIGN.md 第 8 轮 § 8.2 与第 7 轮 § PackageCard 共享 一致,均为 `home` / `list-major` / `list-secondary` 三档。**任务 prompt 顶部提到的 `compact` 别名不采纳**(以 DESIGN.md 第 8 轮为准)。

### 2.3 Props

| 名 | 类型 | 默认 | 必填 | 说明 |
|---|---|---|---|---|
| `pkg` | `Object` | — | ✓ | 套餐对象;shape 见 2.3.1 |
| `variant` | `String` | `'list-major'` | — | validator `['home', 'list-major', 'list-secondary']` |
| `isPurchased` | `Boolean` | `false` | — | 当前用户是否已购买过该套餐;true 时卡顶左上角加"已购" tag(small,主色 ghost),仍允许重复购买 |
| `loading` | `Boolean` | `false` | — | "立即购买" / "立即领取" CTA 等待 Stripe checkout-session 时显示 loading 旋转,disabled 防双击 |

#### 2.3.1 `pkg` shape

| 字段 | 类型 | 来源 | 备注 |
|---|---|---|---|
| `id` | `Number` / `String` | 后端 | 套餐 ID |
| `name` | `String` | 后端(i18n) | 套餐名(已本地化,例 "半年包 2 次/周") |
| `sessions` | `Number` | 后端 | 总课次,例 `52` |
| `periodMonths` | `Number` | 后端 | 有效期月数;免费体验 / 单课用 `periodDays` |
| `periodDays` | `Number` | 后端 | 有效期天数(免费体验 / 单课用),例 `7` |
| `price` | `Number` / `String` | 后端按 currency 返 | 总价金额,组件传给 `<PriceTag size="large">` |
| `currency` | `String` | 后端 | `'HKD' / 'USD' / 'CNY' / ...` |
| `pricePerSession` | `Number` / `String` | 后端 | 单节均价,组件渲染"约 X 节(HK$ Y / 节)" |
| `recommendationLabel` | `String` / `null` | 后端 | 例 `"推荐"`;`null` 不显示 pill |
| `discountLabel` | `String` / `null` | 后端 | 例 `"9 折省 HK$ 200"`;`null` 不显示折扣徽章。⚠️ 完整文案由后端拼,组件不做模板拼接 |
| `sellingPoints[]` | `String[]` | 后端 `package.selling_points` JSON(P1 落) | 例 `['自由预约任选老师', '24h 前可全额退', '课前提醒']`;运营 admin 可配 |
| `badges[]` | `String[]` | 后端预留 | 暂用 `recommendationLabel` / `discountLabel` 显式控制,`badges` P2 启用 |
| `freeTrial` | `Boolean` | 后端 | 免费体验 = true 时,CTA 文案变"立即领取"且不弹确认 dialog,直接走 `/booking/free-trial`;由调用方传入或后端返 |

> 文案 `name` / `sellingPoints` / `recommendationLabel` / `discountLabel` 全部 **i18n 调用方负责**——后端按用户 locale 返,组件直接渲染。

### 2.4 Slots

无对外暴露 slot。

### 2.5 Events

| 名 | payload | 触发场景 |
|---|---|---|
| `click-buy` | `{ pkg }` | "立即购买" CTA 点击;由调用方决定打开确认订单 dialog 还是直接跳 |
| `click-claim-trial` | `{ pkg }` | "立即领取"(免费体验)CTA 点击;由调用方判断登录态后跳 `/register?redirect=/booking/free-trial` 或 `/booking/free-trial` |

### 2.6 视觉 token 映射

| 视觉项 | token |
|---|---|
| 卡片底色 | `var(--color-bg-card)` |
| 圆角 | `brand.$radius-card`(14px) |
| 推荐卡边框 | `1.5px solid brand.$brand-primary`(home / list-major + `recommendationLabel` 非空时);H5 推荐卡 `transform: scale(1.02)` |
| 推荐 pill 背景 | `brand.$brand-primary` + 字 `brand.$color-text-inverse` + `border-radius: brand.$radius-pill`(32px)|
| 推荐 pill 位置 | `inset-block-start: -12px; inset-inline-start: 50%; transform: translateX(-50%)` |
| 折扣徽章背景 | `brand.$color-text-primary`(ink 实底)+ 字 `brand.$color-text-inverse` + `border-radius: brand.$radius-base` |
| 折扣徽章位置 | `inset-block-start: brand.$spacing-3; inset-inline-end: brand.$spacing-3` |
| 套餐名字号 | `brand.$font-size-lg`(18px) + `font-weight: 600` + `color: brand.$color-text-primary` |
| 总价 | `<PriceTag size="large">`(24px primary-deep 600) |
| sellingPoints 字号 | `brand.$font-size-base`(14px) + `color: brand.$color-text-primary`;✓ 用 `var(--color-success)` |
| CTA 推荐卡 | `<el-button type="primary">` 主色实底,全宽,高 48px |
| CTA 非推荐卡 | `<el-button type="default">`(ink 实底白字),全宽,高 48px |
| "已购" tag | `<el-tag size="small" type="warning" effect="plain">`,卡顶左上 `inset-block-start: brand.$spacing-3; inset-inline-start: brand.$spacing-3` |

### 2.7 RTL 注意

- 推荐 pill 顶部居中: `inset-inline-start: 50%; transform: translateX(-50%)`(RTL 时 translate 方向数学等价,无需特殊处理)
- 折扣徽章: `inset-inline-end`(LTR 右上 / RTL 左上)
- "已购" tag: `inset-inline-start`(LTR 左上 / RTL 右上)
- ✓ 卖点行: 内容用 `flex` `gap` 替代 `margin-right`

### 2.8 调用示例

```vue
<!-- HomeView.vue § 4 套餐预览 -->
<template>
  <div class="home-packages">
    <PackageCard
      v-for="p in featuredPackages"
      :key="p.id"
      :pkg="p"
      variant="home"
      @click-buy="goToPackageList"
      @click-claim-trial="claimTrial"
    />
  </div>
</template>

<!-- PackageListView.vue § C 主推 4 卡 -->
<template>
  <div class="package-list__major-grid">
    <PackageCard
      v-for="p in majorPackages"
      :key="p.id"
      :pkg="p"
      variant="list-major"
      :is-purchased="purchasedIds.includes(p.id)"
      :loading="checkoutPending === p.id"
      @click-buy="openConfirmDialog"
    />
  </div>
</template>
```

---

## 3. `<PriceTag>`

### 3.1 用途

多币种价格格式化 + RTL 兼容数字渲染。被 TeacherCard / PackageCard / BookingPanel / 对比表 / 确认订单 dialog 复用,统一价格的视觉调性 + 千分位 + 币种前缀 + "免费"翻译。

### 3.2 variant(在 prop 名上叫 `size`)

| 取值 | 用在哪些页面 | 视觉 |
|---|---|---|
| `default` | `<TeacherCard variant="home/list">` 卡内价格、PackageList 对比表 cell、ProfileView 累计奖励 | 16px primary-deep 600 + period 14px secondary,例 `HK$ 250 / 25 分钟` |
| `large` | `<PackageCard>` 总价、`BookingPanel` 顶部价格、确认订单 dialog 合计 | 24px primary-deep 600,例 `HK$ 1,800` |
| `inline` | 富文本里的内联价格(如订单成功页 toast、邀请记录"奖励 HK$ 50") | inherit 父字号,无 period |

> 注:DESIGN.md 第 8 轮 § 8.4 用的 prop 名是 `size`,不是 `variant`。**保留 `size` 名**,与 EP / Vant 习惯对齐(`<el-input size="large">`);别名 `compare-cell` / `card` / `panel` **不采纳**。

### 3.3 Props

| 名 | 类型 | 默认 | 必填 | 说明 |
|---|---|---|---|---|
| `amount` | `Number` / `String` | — | ✓ | `String` 用于 BigDecimal 高精度场景(后端 DECIMAL(12,2) 直传字符串避免 JS 浮点误差) |
| `currency` | `String` | `'HKD'` | — | validator `['HKD', 'USD', 'CNY', 'GHS', 'AED']`,M5+ 海外加币种时扩 validator |
| `period` | `String` | `''` | — | 例 `/ 25 分钟` / `/ 节`;**i18n 调用方** 用 `t()` 拼好传入,空字符串不渲染 |
| `size` | `String` | `'default'` | — | validator `['default', 'large', 'inline']` |
| `free` | `Boolean` | `false` | — | 强制显示"免费"翻译;`amount === 0` 时也自动显示 |
| `discountFromAmount` | `Number` / `String` / `null` | `null` | — | (P2 预留,P0/P1 不传)用于对比划线价场景(`<del>HK$ 2000</del> HK$ 1800`)。无值时不渲染删除线 |

### 3.4 格式化规则(组件内置)

| currency | 前缀 | 示例 |
|---|---|---|
| `HKD` | `HK$` | `HK$ 1,800` |
| `USD` | `US$` | `US$ 230` |
| `CNY` | `¥` | `¥ 1,500` |
| `GHS` | `GH₵` | `GH₵ 2,800`(M5+) |
| `AED` | `AED ` | `AED 850`(M5+) |

实现:
```js
const formatted = new Intl.NumberFormat(locale, { style: 'decimal' }).format(amount)
const display = `${prefix}${formatted}`   // 前缀手动拼,避免浏览器返 "HKD" 而非 "HK$"
```

### 3.5 "免费"翻译(组件内置 — i18n § 0.3 白名单例外)

| locale | 显示 |
|---|---|
| `zh-CN` | 免费 |
| `zh-TW` | 免費 |
| `en` | Free |
| `ar` | مجانًا |

i18n key 定为 `common.price.free`(单一 key 4 套语言)。

### 3.6 Slots

无。

### 3.7 Events

无。

### 3.8 视觉 token 映射

| size | 字号 | 颜色 | period 字号 / 颜色 |
|---|---|---|---|
| `default` | `brand.$font-size-md`(16px) + `font-weight: 600` | `brand.$brand-primary-deep` | `brand.$font-size-base`(14px) + `brand.$color-text-secondary` |
| `large` | `brand.$font-size-2xl`(24px) + `font-weight: 600` | `brand.$brand-primary-deep` | 同 default,但与金额同行内 |
| `inline` | `inherit` | `inherit` | 无 period |

### 3.9 RTL 注意

- 容器 `text-align: end`(默认),数字保持 LTR(浏览器 bidi 自动 — `HK$ 1,800` 在 RTL 容器内仍正常渲染)
- `discountFromAmount` 划线价: `<del>` 用 `margin-inline-end: brand.$spacing-2`,不要 `margin-right`

### 3.10 调用示例

```vue
<!-- PackageCard 内 -->
<PriceTag :amount="pkg.price" :currency="pkg.currency" size="large" />

<!-- BookingPanel 顶部 -->
<PriceTag :amount="250" currency="HKD" period="/ 25 分钟" size="large" />

<!-- 免费体验 -->
<PriceTag :amount="0" currency="HKD" />
<!-- 渲染 "免费" / "Free" / "مجانًا" 按 locale 自动 -->

<!-- inline -->
<p>本月已奖励 <PriceTag :amount="50" currency="HKD" size="inline" /></p>
```

---

## 4. `<EmptyState>`

> ⚠️ **本组件已由 Wave 1.5.4 实现**(`app/src/components/EmptyState.vue`),本节为契约**反向锁定**——发现实现与本契约冲突时,**以 SHARED-API.md 为准 + 同步改实现 + PR 注明**。

### 4.1 用途

通用空态(列表 0 结果 / 无数据 / 网络错误 / 无收藏),全站统一空态视觉调性。Wave 4 TeacherList 搜索筛选 0 结果、Wave 2 TeacherDetail 无评价、Wave 3 Profile 无邀请、MyOrders 6 个空 tab、网络错误 retry 全部用本组件。

### 4.2 variant

| 取值 | 默认图标(EP icon) | 用在哪些页面 |
|---|---|---|
| `no-data` | `Box` | TeacherDetail 无评价、MyOrders 空 tab、Profile 无邀请、通用"暂无数据" |
| `no-result` | `Search` | TeacherList 搜索 / 筛选 0 结果、PackageList 币种切换后无包(P2)|
| `error` | `WarningFilled` | 网络错误 / API 失败 retry 兜底(图标外加 8px error 红晕) |
| `no-favorite` | `Star` | P1+ 收藏夹空(MyFavorite 路由,P0/P1 不实现) |

> 注:DESIGN.md 第 8 轮 § 8.3 用的是 `size: 'default' | 'compact'` 两档(场景靠"用法表"区分);**Wave 1.5.4 实现演化为 `variant: no-data/no-result/error/no-favorite` + `compact: Boolean`**,语义化更强(场景 = variant,密度 = compact)。**本契约采纳 Wave 1.5.4 实现的形态**,DESIGN.md 第 8 轮的 `size` 字段视为已被 superseded。

### 4.3 Props

| 名 | 类型 | 默认 | 必填 | 说明 |
|---|---|---|---|---|
| `variant` | `String` | `'no-data'` | — | validator `['no-data', 'no-result', 'error', 'no-favorite']` |
| `icon` | `String` / `Object` | `''` | — | EP icon name(string,如 `'Search'`)或组件实例;空时按 variant 兜底(见 4.2) |
| `title` | `String` | — | ✓ | 空态主标题,**i18n 调用方** |
| `description` | `String` | `''` | — | 副描述,**i18n 调用方**,空时不渲染 |
| `actionText` | `String` | `''` | — | 主 CTA 按钮文案,**i18n 调用方**;空 + `actionLink` 空时不渲染按钮 |
| `actionLink` | `String` / `Object` | `''` | — | `<router-link>` `to` 字段;字符串路径或 location 对象。与 `actionText` 配套 |
| `compact` | `Boolean` | `false` | — | 紧凑模式:嵌入 tab / 卡片 / 抽屉时高度 280px → 160px,无暖米底,图标 64px |

### 4.4 Slots

| 名 | 用途 | 优先级 |
|---|---|---|
| `icon` | 自定义图标(替代 EP icon),例:品牌插画 SVG | 优先级最高,覆盖 props.icon 与 variant 兜底 |
| `action` | 自定义 CTA 区(支持多按钮 / 双 CTA);例:`<el-button>清除筛选</el-button> <el-button type="primary">浏览全部</el-button>` | 优先级高于 props.actionText / actionLink;有 slot 时忽略默认按钮 |

### 4.5 Events

无(交互通过 slot.action 内 `<el-button @click>` 自定义,或 `<router-link>` 跳转)。

### 4.6 主要使用场景文案表(参考 DESIGN.md 第 8 轮 § 8.3)

| 场景 | variant | icon(prop 或 兜底) | title 例 | description 例 | CTA |
|---|---|---|---|---|---|
| TeacherList 筛选 0 结果 | `no-result` | `Search` | 没有找到匹配的老师 | 试试放宽筛选条件 | 清除筛选 / 浏览全部(双按钮 → `#action` slot)|
| MyOrders 空 tab | `no-data` | `Calendar` | 暂无 X 订单 | — | 浏览老师(仅 upcoming/all tab) |
| TeacherDetail 无评价 | `no-data` | `ChatLineRound` | 暂无评价 | 成为第一位评价的学员 | — |
| Referral 无邀请 | `no-data` | `Promotion` | 还没有邀请过朋友 | 复制邀请链接分享 | 复制链接 |
| 网络错误 | `error` | `WarningFilled`(兜底) | 加载失败 | 请检查网络后重试 | 重试 |
| MyFavorite 空(P1+) | `no-favorite` | `Star`(兜底) | 还没有收藏的老师 | — | 浏览老师 |

### 4.7 视觉 token 映射

| 视觉项 | token |
|---|---|
| 容器底(default) | `var(--color-primary-soft)`(暖米底) |
| 容器底(compact) | `transparent`(无底) |
| 圆角 | `var(--radius-lg)`(12px) |
| 容器最小高度 default / compact | 280px / 160px |
| 图标圆形包装(default) | `120 × 120` + `var(--radius-full)` + `var(--color-bg-card)` 底 + `var(--shadow-base)` |
| 图标圆形包装(compact) | `64 × 64` + 无底 + 无阴影 |
| 图标本身字号 | 56px(default)/ 32px(compact) |
| 图标颜色 | `var(--color-primary)` |
| 标题字号 | `brand.$font-size-lg`(18px) + `font-weight: 600` + `color: var(--color-text-primary)` |
| 描述字号 | `brand.$font-size-base`(14px) + `color: var(--color-text-secondary)` + `max-width: 360px` |
| error variant 8px 红晕 | `box-shadow: 0 0 0 8px rgba(255, 77, 79, 0.08)` ⚠️ M5 后补 `--color-error-soft` token 替换字面量 |

### 4.8 RTL 注意

- 容器 `text-align: center` — 不受 RTL 影响
- `padding-block` / `padding-inline` 用 logical properties,实现已就位
- action 区 `flex` `gap`,无 `margin-left/right`,RTL 镜像自动

### 4.9 a11y

- 容器 `role="status" aria-live="polite"` — 实现已就位
- error variant 不用 `role="alert"`(避免打断屏幕阅读器朗读)

### 4.10 调用示例

```vue
<!-- TeacherList 筛选 0 结果 -->
<EmptyState
  variant="no-result"
  :title="t('teachers.empty.title')"
  :description="t('teachers.empty.desc')"
>
  <template #action>
    <el-button @click="resetFilters">{{ t('teachers.empty.clear') }}</el-button>
    <el-button type="primary" @click="browseAll">{{ t('teachers.empty.browseAll') }}</el-button>
  </template>
</EmptyState>

<!-- MyOrders 空 tab(紧凑) -->
<EmptyState
  variant="no-data"
  :title="t('orders.empty.upcoming')"
  :action-text="t('orders.empty.browseTeachers')"
  :action-link="{ name: 'TeacherList' }"
  compact
/>

<!-- 网络错误 -->
<EmptyState
  variant="error"
  :title="t('common.networkError.title')"
  :description="t('common.networkError.desc')"
>
  <template #action>
    <el-button type="primary" @click="retry">{{ t('common.retry') }}</el-button>
  </template>
</EmptyState>
```

---

## 5. `<StatCard>`

### 5.1 用途

数字统计卡(数字 + 标签 + 可选图标 / 单位 / 趋势),被 ProfileView 推荐战绩 / 教学统计、未来 admin dashboard 等复用。

文件位置: `app/src/components/profile/StatCard.vue`(profile 子目录,与第 4 轮 ProfileView 影响文件清单一致)。

### 5.2 variant(在 prop 名上叫 `size`)

| 取值 | 用在哪些页面 | 视觉 |
|---|---|---|
| `default` | `app/src/views/profile/ReferralView.vue` 3 数字卡(已邀请 / 已奖励 / 累计奖励)、`app/src/views/profile/TeacherStatsView.vue` 3 数字卡(平均评分 / 已完成课时 / 评价数) | 双行:value 24px ink 600 + label 14px secondary,有边框 + `radius-lg` |
| `compact` | 卡片内 / 抽屉内嵌入,或 admin dashboard 简版 | 单行:`value label`,无边框 |

> 注:任务 prompt 顶部提到的"单数字 / 数字+趋势 / 数字+图标"三 variant **不采纳**(以 DESIGN.md 第 8 轮 § 8.5 为准 — `default` / `compact` 两档,趋势 / 图标通过 prop 控制而非 variant)。这样更正交:variant 控制密度,prop 控制内容元素。

### 5.3 Props

| 名 | 类型 | 默认 | 必填 | 说明 |
|---|---|---|---|---|
| `value` | `String` / `Number` | — | ✓ | 主数字,例 `4.9` / `580` / `50`;**调用方负责**金额类用 `<PriceTag>` 替代或预先格式化(本组件不做币种格式化) |
| `label` | `String` | — | ✓ | 描述文字,例"平均评分",**i18n 调用方** |
| `icon` | `String` / `Object` | `null` | — | EP icon name 或组件;`null` / 不传 时不渲染图标位 |
| `prefix` | `String` | `''` | — | value 前缀,例 `⭐` / `HK$`;空字符串不渲染 |
| `suffix` | `String` | `''` | — | value 后缀单位,例 `%` / `节`;空字符串不渲染 |
| `size` | `String` | `'default'` | — | validator `['default', 'compact']` |
| `trend` | `Object` / `null` | `null` | — | (P2 预留,P0/P1 不传)趋势对象 `{ direction: 'up' \| 'down' \| 'flat', delta: '+12%' }` |
| `color` | `String` | `'default'` | — | (可选,P1+)语义色 `'default' \| 'primary' \| 'success' \| 'warning' \| 'error'`;`default` 时 value 用 ink,`primary` 时 value 用主色-deep |

### 5.4 Slots

| 名 | 用途 |
|---|---|
| `value` | 自定义 value 渲染(例:嵌入 `<PriceTag>`)。优先级高于 props.value |
| `extra` | 卡底额外内容(例:P2 趋势小箭头 + 百分比) |

### 5.5 Events

无。

### 5.6 视觉 token 映射

| 视觉项 | token |
|---|---|
| 容器边框(default) | `1px solid brand.$color-border` |
| 容器圆角(default) | `brand.$radius-lg`(12px) |
| 容器 padding(default) | `brand.$spacing-4 brand.$spacing-6` |
| 容器边框(compact) | 无 |
| 容器底色 | `var(--color-bg-card)` |
| value 字号(default) | `brand.$font-size-2xl`(24px) + `font-weight: 600` + `color: brand.$color-text-primary` |
| value 字号(compact) | `brand.$font-size-lg`(18px) + `font-weight: 600` |
| label 字号 | `brand.$font-size-base`(14px) + `color: brand.$color-text-secondary` |
| prefix(`⭐` 等) | 与 value 同字号同行 |
| icon 字号 | 24px,主色 `var(--color-primary)`,与 value 同行,`margin-inline-end: brand.$spacing-2` |

### 5.7 RTL 注意

- prefix + value + suffix 同行: `flex` `gap: brand.$spacing-2`,无物理 margin
- icon 与 value 间距: `margin-inline-end`(LTR / RTL 自动)
- compact 单行: 整体 `text-align: start`(LTR 左 / RTL 右),与表格 cell 对齐习惯一致

### 5.8 调用示例

```vue
<!-- ProfileView referral 3 数字卡 -->
<template>
  <div class="referral-stats">
    <StatCard :value="3" :label="t('profile.referral.stat.invited')" />
    <StatCard :value="2" :label="t('profile.referral.stat.rewarded')" />
    <StatCard prefix="HK$" :value="50" :label="t('profile.referral.stat.totalReward')" />
  </div>
</template>

<!-- ProfileView teacherStats -->
<template>
  <div class="teacher-stats">
    <StatCard prefix="⭐" :value="4.9" :label="t('profile.teacherStats.avgRating')" />
    <StatCard :value="580" :label="t('profile.teacherStats.completedLessons')" suffix="节" />
    <StatCard :value="120" :label="t('profile.teacherStats.reviewCount')" />
  </div>
</template>

<!-- compact(嵌入抽屉) -->
<StatCard :value="24" :label="t('balance.remaining')" suffix="节" size="compact" />

<!-- 用 slot 嵌入 PriceTag(避免双重格式化) -->
<StatCard :label="t('profile.referral.stat.totalReward')">
  <template #value>
    <PriceTag :amount="50" currency="HKD" size="large" />
  </template>
</StatCard>
```

---

## 附录 A — 多 subagent 协作纪律

### A.1 Wave 2 — Subagent A / B 同时引用 TeacherCard / PackageCard

- **Subagent B 落实现**(`TeacherCard.vue` / `PackageCard.vue` / `PriceTag.vue` 创建于 Wave 2 P0-6 HomeView 改造)
- **Subagent A 只 import 不改 API**(P0-7 / P0-8 业务页引用现成组件)
- 发现 prop 不够用时,**先在本文件加 prop 注明 P1+ 占位 + 默认值**,再修改实现;不允许私自加 prop 不更新本文件

### A.2 Wave 3 — Subagent C / D 引用 EmptyState / StatCard

- `EmptyState` 已由 Wave 1.5.4 落地,Wave 3 直接 import 使用
- `StatCard` 由 Wave 3 P0-7 ProfileView 改造时首次落地;后续 admin dashboard 复用

### A.3 Wave 4 — Subagent E / F 复用全部组件

- 发现 API 不够用 → **PR 描述里写"提议扩展 SHARED-API.md"** + 在本文件加 prop / variant + commit message 标 `feat(SHARED-API): ...` → 再修改实现 + 引用方
- **不要私自 hack 实现绕开契约**(例:在 TeacherCard 里 v-if class 加新样式分支)

### A.4 破坏性变更流程

修改本文件中已存在的 prop 名 / 类型 / 默认值 / variant 取值,或删除组件能力 → **破坏性变更**:

1. PR 描述顶部 `## ⚠️ BREAKING SHARED-API.md`(grep 关键字,人工 review 抓得到)
2. commit message 标 `BREAKING CHANGE:` 一行
3. 同 PR 修改所有引用方(grep `<TeacherCard|<PackageCard|<PriceTag|<EmptyState|<StatCard` 找下游)
4. 在本文件 § 历史变更 末尾加一条记录(日期 + commit hash + 改了什么)

非破坏性新增(加可选 prop / 加 variant 取值)→ 直接更新本文件 + 实现 + commit message `feat(SHARED-API):`,无需特殊流程。

---

## 附录 B — 历史变更

| 日期 | commit | 变更 |
|---|---|---|
| 2026-05-10 | (Wave 1.5.5 创建) | 初版,5 组件契约;EmptyState 采纳 Wave 1.5.4 实现的 `variant: no-data/no-result/error/no-favorite + compact:Boolean`,supersede DESIGN.md 第 8 轮 § 8.3 的 `size: default/compact` |
