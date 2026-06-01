---
status: active
type: reference
domain: frontend
---

# Mandarly 前端规范目录

> 维护原则:本目录是**项目级前端规范唯一源**,admin / app / 邮件模板 / 社交物料的视觉决策必须在这里有据可查。
> 上游业务源:`../product/prd-v4.md`(业务) + `../brand/deliverables-v1/`(品牌资产)

## 目录结构

```
docs/frontend/
├── README.md                       ← 本文,目录索引(规范读法)
├── UI风格规范-v1.md                 ← 工程契约(token / 组件库 / RTL / i18n / 时区 / Checklist)
├── UX审计与优化-v1.md               ← 用户端(app/)双端 UX 审计 + P0-P3 优化路线图(2026-05-09)
├── SEO-GEO-清单.md                  ← Google SEO + LLM Agent GEO 优化清单(公开页面必读)
└── 视觉参考/                        ← 视觉样板与参考(组件级视觉决策)
    ├── README.md
    ├── DESIGN-mandarly-v1.md        ← Mandarly 适配版 DESIGN.md(主用,AI 协作首选)
    └── DESIGN-airbnb-原版.md         ← Airbnb 原版镜像(只读,追溯设计哲学时翻)
```

## 文档分层

| 层 | 文件 | 谁读 | 解决什么 |
|---|---|---|---|
| **工程契约** | `UI风格规范-v1.md` | 所有前端工程师 / Code Review / OPC | 用什么 token / 选哪个组件库 / RTL 怎么编码 / i18n 文案落库规则 / 时区如何显示 / 上线 Checklist |
| **UX 审计 + 路线图** | `UX审计与优化-v1.md` | OPC / 排期决策 / 上线前 review | 当前 app/ 用户端双端体验偏离规范的地方、各页诊断、P0-P3 优化路线、Vant 路线决策、双端 Checklist 补充。**本文会过期**:落完整轮 P0 后冻结归档。 |
| **视觉样板** | `视觉参考/DESIGN-mandarly-v1.md` | 前端工程师 / AI 协作 / 设计协作 | 按钮卡片各状态长什么样 / 字体阶梯映射 / 间距落点 / 组件级视觉决策 |
| **样板原版** | `视觉参考/DESIGN-airbnb-原版.md` | 设计追溯 / 范式参考 | 当 Mandarly 适配版有歧义时,回查原 marketplace 哲学 |
| **SEO/GEO 清单** | `SEO-GEO-清单.md` | 写公开页面(`/` `/teachers` `/teacher/:id` `/level-check`)前必读 | meta 怎么写 / `<router-link>` 替代 `@click` / JSON-LD / robots.txt + llms.txt / sitemap / 大改 backlog |

**为什么分两层**:
- 工程契约关注"**怎么做不会错**"(token、纪律、Checklist)
- 视觉样板关注"**做出来长什么样**"(组件视觉、字体阶梯、状态)
- 工程契约稳定低频(改一次 bump 主版本),视觉样板高频迭代(每加业务卡片就增补)

## 阅读顺序(写新页面前)

1. **`../product/prd-v4.md`** 找业务章节 §X.Y → 知道页面要表达什么、有哪些数据
2. **`UI风格规范-v1.md`** §1-§9 → 锁定 token、组件库、RTL/i18n/时区编码纪律
3. **`视觉参考/DESIGN-mandarly-v1.md`** → 找最贴近的组件 token,直接拿配色 / 字号 / 间距 / 状态
4. **`UI风格规范-v1.md` §10 Checklist** → 提交前自检
5. 涉及多语言文案 → `app/src/i18n/locales/{en,zh-CN,zh-TW,ar}.js` 4 套同步增删

## AI 协作约定

让 Claude / Codex 写前端时,prompt 第一行附上:

> 写之前先读 `docs/frontend/ui-style-guide-v1.md`(工程契约) + `docs/frontend/visual-reference/DESIGN-mandarly-v1.md`(视觉样板),按这两份文件落地。

不要让 AI 直接看 Airbnb 原版——色相 / 字体 / RTL 都没翻译过来,容易跑偏。

## 维护规则

| 修改场景 | 改哪份 | 是否 bump 版本 |
|---|---|---|
| 增加 / 调整 design-token(颜色、字号、间距) | `UI风格规范-v1.md` §1-§4 + `视觉参考/DESIGN-mandarly-v1.md` frontmatter | 视觉样板 bump 小版本 |
| 增加业务卡片 / 组件视觉(教师卡新加 ribbon、套餐卡变体) | `视觉参考/DESIGN-mandarly-v1.md` components 段 | 视觉样板小版本 |
| 调整组件库选型 / RTL 纪律 / Checklist 项 | `UI风格规范-v1.md` 对应章节 | 工程契约小版本 |
| 主色 / 字体 / 整体调性大改 | 先改 `../product/prd-v4.md` §13 → 回流两份 | 工程契约 + 视觉样板都 bump 主版本(v2) |
| 新增视觉参考样板(如再镜像一份 Notion / Cal.com) | `视觉参考/` 加新文件 + 更新本 README + 更新 `视觉参考/README.md` | 不影响版本 |

## 历史版本

- `UI风格规范-v1.md` · 2026-05-06 首版
- `视觉参考/DESIGN-mandarly-v1.md` · 2026-05-06 首版(基于 Airbnb 范式)
