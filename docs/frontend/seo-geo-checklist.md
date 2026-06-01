# SEO + GEO 清单(Google 搜索 + LLM Agent 可见性)

> 起源:2026-05-08 用户要求「写代码时 Google SEO 和 LLM agent GEO 能搜到」(不强制实现)
> 范围:`app/`(用户端公开页面);admin 后台不需要 SEO
> 状态:**约束 + 示范代码已就位,深度优化 backlog**

## 0. 总原则

1. **不强制重构** — 后续公开页面新建/修改时按清单做,不为 SEO 单独发版
2. **零成本项必做** — 加 meta / alt / 语义化标签 / robots.txt / llms.txt 这些不增加工作量的项,任何 PR 都顺手做
3. **大改留 backlog** — SSR / prerender / 多语言 URL 重构这些大动作,业务量起来或 GA 上线前再评估
4. **覆盖范围**:
   - **公开页面(必做 SEO)**:`/` 首页 / `/teachers` 教师列表 / `/teacher/:userId` 教师详情 / `/level-check` 水平测试
   - **私有页面(不做)**:`/orders` / `/profile` / `/review/:id` / `/my/packages` / `/classroom/:id` 等需登录页
   - **关闭页面(不索引)**:`/login` / `/register` / `/auth/callback/*` / `/classroom-stub` 等

## 1. SEO 清单(Google / Bing 传统搜索)

### 1.1 元数据(每个公开页面必有)

- [ ] `<title>` 50-60 字符,含主关键词(中文「中文培训 / 学中文 / 1对1」、英文「Learn Mandarin Online / 1-on-1 Chinese tutor」)
- [ ] `<meta name="description">` 150-160 字符,每页不同
- [ ] `<meta name="keywords">`(权重低但加上无害)
- [ ] `<link rel="canonical">` 防 `?utm=` 等参数被当成不同页索引
- [ ] `<html lang="...">` 跟随一期启用 i18n locale 切换(`en` / `zh-CN` / `zh-TW`);阿语二期开放前不对外声明
- [ ] `<meta name="robots" content="index, follow">` 公开页;`noindex` 用于私有

### 1.2 Open Graph + Twitter Card(分享卡片)

- [ ] `og:title` / `og:description` / `og:image`(用 `docs/brand/deliverables-v1/02-cover/cover-mandarly-1500x500.png`)
- [ ] `og:url` / `og:type=website`(首页)/ `profile`(教师页)
- [ ] `og:locale=en_US`(默认主语言英文,动态页跟随 i18n)/ `og:locale:alternate=zh_CN/zh_TW`
- [ ] `twitter:card=summary_large_image`

### 1.3 语义化 HTML

- [ ] `<header>` / `<nav>` / `<main>` / `<article>` / `<section>` / `<footer>`,**不要全用 `<div>`**
- [ ] 每页 1 个 `<h1>`,层级清晰 h1→h2→h3,不跳级
- [ ] `<a href="...">` 真链接(SPA 用 `<router-link>` 已经 OK),**不要用 `<button @click="router.push">`** 跳页(爬虫不识别)
- [ ] `<img alt="...">` 图片必填 alt,描述图片内容(教师头像 alt="教师 张老师 头像")
- [ ] `<button type="button">` 明确类型,避免提交副作用

### 1.4 结构化数据(JSON-LD)

公开页面在 `<head>` 嵌 `<script type="application/ld+json">`,告诉 Google 这是什么:

| 页面 | Schema 类型 |
|---|---|
| `/` 首页 | `Organization` + `EducationalOrganization` |
| `/teachers` 列表 | `ItemList`(每条 `Person`)|
| `/teacher/:userId` | `Person`(教师)+ `AggregateRating`(评分)+ `Course`(其教授内容)|
| `/level-check` | `Quiz`(测试)|
| 评价区(嵌入教师页) | `Review` + `AggregateRating` |

最小可行示例(教师页 JSON-LD):

```html
<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "Person",
  "name": "Lily(Business)",
  "jobTitle": "Mandarin Tutor",
  "image": "http://localhost:3001/avatars/2001.jpg",
  "worksFor": {
    "@type": "EducationalOrganization",
    "name": "Mandarly"
  },
  "aggregateRating": {
    "@type": "AggregateRating",
    "ratingValue": "5.0",
    "reviewCount": "1"
  }
}
</script>
```

### 1.5 性能(Core Web Vitals,Google 排名因子)

- [ ] LCP < 2.5s(首页主图懒加载;`<img loading="lazy">`)
- [ ] CLS < 0.1(图片必带 width/height,避免布局抖动)
- [ ] INP < 200ms(避免重计算阻塞主线程)
- [ ] 图片用 WebP / AVIF 而非 PNG/JPG(brand 资源已是 PNG,后续可加 WebP fallback)

### 1.6 多语言(i18n)

- [ ] `<link rel="alternate" hreflang="zh-CN" href="http://localhost:3001/zh/...">`,告诉 Google 是同页面的不同语言版本
- [ ] hreflang `x-default` 指向英文(国际兜底)
- [ ] **理想路径:`/zh/teachers` `/en/teachers`**(URL 区分语言;繁体可用 `/zh-hant/teachers` 或同等约定);**当前是 SPA `?lang=` 或 i18n state**,不利 SEO;留 backlog 重构。阿语 `/ar/*` 留二期

### 1.7 sitemap.xml + robots.txt

- [ ] `public/robots.txt`:允许 Googlebot / Bingbot / GPTBot / ClaudeBot,禁止私有路径
- [ ] `public/sitemap.xml`:列出所有公开 URL,定期更新(教师列表动态生成,后续接 `/sitemap.xml` 后端接口)

## 2. GEO 清单(LLM Agent 搜索可见性)

> ChatGPT / Claude / Perplexity / Google AI Overviews 等 LLM agent 搜索时偏好的格式。
> 行业还没有统一标准,但有几个共识。

### 2.1 llms.txt(类似 robots.txt 给 LLM 用)

放 `public/llms.txt`,告诉 LLM:
- Mandarly 是什么(在线 1v1 中文培训)
- 主要公开页面 URL
- 教师列表 / 套餐价格 / 注册流程

格式参考 `https://llmstxt.org`(标准草案,Markdown 风格)。

### 2.2 内容结构(对 LLM 友好)

- [ ] **FAQ schema**(`<script type="application/ld+json"> { "@type": "FAQPage" }`)— 让 ChatGPT / Google AI Overviews 直接引用
- [ ] **清晰事实陈述**:不绕弯子,「Mandarly 提供香港和海外华人 1v1 中文培训,主打口语和 HSK」这种段落 LLM 会直接抓
- [ ] **可被引用**:加 `<cite>` / `<blockquote>`,避免「视频/动图」依赖
- [ ] **结构化列表 + 表格**:Markdown 风格的内容(教师介绍 / 套餐对比 / 价格表)LLM 解析快
- [ ] **不要混淆主体**:每页 H1 + 第一段就把「这页是关于什么 / Mandarly 是什么」交代清楚

### 2.3 不要屏蔽 AI 爬虫

robots.txt 里 **明确放行** 这些 user-agent(2026 年常见):

```
User-agent: GPTBot         # OpenAI / ChatGPT
User-agent: ClaudeBot      # Anthropic / Claude
User-agent: ChatGPT-User
User-agent: PerplexityBot
User-agent: Google-Extended # Google AI Overviews / Gemini
User-agent: anthropic-ai
User-agent: cohere-ai
Allow: /
Disallow: /orders
Disallow: /profile
...
```

### 2.4 引擎差异(2026 实测共识)

| 引擎 | 偏好 |
|---|---|
| ChatGPT | 结构化(bullet / FAQ / 表格)|
| Claude | 长段落 + 连贯论述 |
| Perplexity | 权威来源 + 引用链接 |
| Google AI Overviews | FAQ / HowTo schema + 短定义 |

**实施策略**:写公开内容时**结构化为主**(列表/表格)+ **段落首句下定义**。

## 3. Mandarly 实施分级

### 3.1 ✅ 零成本立即做(已落地或本次落地)

- [x] `app/index.html` 默认 meta 升级(og / twitter / robots / canonical)— **本次落地**
- [x] `app/public/robots.txt`(放行 GPT/Claude/Google bots,禁私有路径)— **本次落地**
- [x] `app/public/llms.txt`(Mandarly 概述给 LLM)— **本次落地**
- [x] `app/public/sitemap.xml`(初版静态)— **本次落地**

### 3.2 🟡 后续公开页面新建/修改时顺手做(零强制)

- [x] `useHead`(`@vueuse/head`)动态 title / description / og(每页不同)— **2026-05-08 落地**(`/` `/teachers` `/teacher/:id` `/level-check` `/level-check/result/:id`,详见 `app/src/utils/seo.js`)
- [x] 每个公开页加 1 段结构化数据 JSON-LD — **2026-05-08 落地**:
  - `/` Organization + WebSite + SearchAction(叠加 index.html 的 EducationalOrganization)
  - `/teachers` ItemList(每条 Person mini-card)
  - `/teacher/:id` Person + AggregateRating + Course + Review ItemList(评价区)
  - `/level-check` Quiz
- [x] `<router-link>` 替换公开页跳页 `@click="router.push"` — **2026-05-08 落地**:HomeView CTA / TeacherList 卡片 + tl-back / TeacherDetail td-back / ResultView 推荐教师 + 浏览更多
- [x] **后端动态 sitemap.xml** — **2026-05-08 落地**:`PublicSitemapController` `/app-api/edu/sitemap.xml`,nginx `location = /sitemap.xml` 反代生产;静态页 + 动态教师 URL(`listVisibleTeachers()`),首页 hreflang 5 语;dev 期 `app/public/sitemap.xml` 作 fallback
- [ ] 教师页用 `<article>`,评分用 `<dl>` + AggregateRating(部分,AggregateRating 已进 JSON-LD;微数据可选)
- [ ] 图片加 `alt` + `loading="lazy"` + `width/height`(教师头像目前是首字母 placeholder,真实头像上线时同步加)

#### 📊 Lighthouse SEO 基线(2026-05-08,本地 dev :3001,4 公开 URL)

| URL | SEO score | 主要 fail |
|---|---|---|
| `/` | **100** | — |
| `/teachers` | **100** | — |
| `/teacher/2001` | **100** | — |
| `/level-check` | **100** | — |

跑法:`npx lighthouse@12 http://localhost:3001/<path> --only-categories=seo --output=json --quiet --chrome-flags="--headless=new"`
说明:dev 跑分 100 不代表生产 100,Core Web Vitals(LCP/CLS/INP)需上线后用 PSI 真测;Rich Results Test 上线后再扫一次 schema 是否被 Google 识别。

### 3.3 🔴 大改 backlog(待业务量起来评估)

- [ ] **SSR / prerender**:Vite SSR 或 vite-plugin-prerender 给公开页预渲染静态 HTML(教师列表 / 教师详情 / 首页),登录后页面不变
- [ ] **多语言 URL 重构**:`/zh/teachers` `/en/teachers` + 繁体路径,加 hreflang;`/ar/*` 留二期
- [ ] **图片 WebP / AVIF**:vite-imagetools 自动生成多格式
- [ ] **后端 sitemap 接口**:`GET /sitemap.xml` 动态拼教师列表
- [ ] **CDN + Edge 缓存**:Cloudflare / 阿里云 CDN(M5 上线时)
- [ ] **GA / GSC 接入**:Google Analytics 4 + Search Console(M5 上线时)

## 4. 写代码时的具体做法

### 4.1 任何新公开 view 创建

```vue
<script setup>
import { useHead } from '@vueuse/head' // 装了的话

useHead({
  title: '教师列表 · Mandarly',
  meta: [
    { name: 'description', content: '...' },
    { property: 'og:title', content: '...' },
    { property: 'og:image', content: '/cover.png' }
  ]
})
</script>

<template>
  <main>
    <h1>...</h1>
    <article v-for="t in teachers" :key="t.id">
      <h2>{{ t.nickname }}</h2>
      ...
    </article>
  </main>
</template>
```

### 4.2 任何跳转

```vue
<!-- ❌ -->
<button @click="router.push(`/teacher/${t.id}`)">查看</button>

<!-- ✅ -->
<router-link :to="`/teacher/${t.id}`" class="btn">查看</router-link>
```

### 4.3 任何图片

```vue
<!-- ❌ -->
<img :src="t.avatar" />

<!-- ✅ -->
<img :src="t.avatar" :alt="`${t.nickname} 教师头像`" loading="lazy" width="120" height="120" />
```

### 4.4 任何文案

写文章 / 教师介绍 / 套餐说明时:
- 首句下定义(「Mandarly 是面向香港及海外华人的在线中文 1v1 培训平台」)
- 列表式呈现(套餐价格 / 教师特长 / 课程亮点用 `<ul>` / `<table>`)
- 自然嵌主关键词(中文培训 / 学中文 / Mandarin tutor)不堆砌

## 5. 触发约束(给 Claude / Codex / 其他 agent)

任何涉及 `app/` 公开页面(`/` `/teachers` `/teacher/:id` `/level-check`)新建或修改时,**先读本文档**(类似 `docs/frontend/ui-style-guide-v1.md` 的强约束)。

零成本项(meta / alt / 语义化标签 / `<router-link>`)直接做,不需要单独 PR。

大改(SSR / 多语言 URL)需用户明确启动,不主动重构。

## 6. 参考来源

- [Generative Engine Optimization (GEO): The 2026 Guide to AI Search Visibility — LLMrefs](https://llmrefs.com/generative-engine-optimization)
- [Wikipedia — Generative engine optimization](https://en.wikipedia.org/wiki/Generative_engine_optimization)
- [Best GEO Strategies in 2026 — Connor Kimball](https://connorkimball.com/blog/best-generative-engine-optimization-geo-strategies/)
- [GEO and the LLMs.TXT File — Andrew Coyle](https://www.andrewcoyle.com/blog/generative-engine-optimization-and-the-llms-txt-file)
- [GEO: Generative Engine Optimization — arXiv 2311.09735](https://arxiv.org/pdf/2311.09735)
- [llmstxt.org 标准草案](https://llmstxt.org)
- [schema.org](https://schema.org)
- [Google Search Central — Search Essentials](https://developers.google.com/search/docs)
