# 03 · 支付 — 接入设计复核报告

> ✅ **本报告 12 项已被 M4 spec / plan 处理(2026-05-09 同步,见底部状态表)**
>
> 复核对象 `docs/database/03-payment.md` v1.1 已在同日废弃,**新设计在** `docs/progress/m4-stripe-payment.md` + `docs/progress/m4-stripe-payment-plan.md`。本报告作为"复核 → 修订"过程审计保留,**不再作为待办输入**。

---

> **复核范围**: `docs/database/03-payment.md` v1.1 + `server/sql/mysql/mandarly.sql` 中的 `payment` / `refund` / `payment_webhook_event` 三张表
> **参考依据**: Stripe 官方 Java sample (`/Users/liuguanye/home/Develop/project/payment-refs/accept-a-payment/`) + Stripe 官方 API 文档 + 跨境支付实战经验
> **复核日期**: 2026-05-08
> **状态**: ✅ 已并入 M4(2026-05-09 主 agent 复核 → 拍板 → patch + plan/spec 同步)
> **结论摘要**: 架构层面优秀(Webhook 幂等、ref_intent 强校验、状态分离均到位),但在 **Stripe 单位制、Dispute 事件、Idempotency、PaymentIntent 生命周期同步** 等"实战细节"上有明显遗漏,共 12 项问题,其中 3 项必改、6 项强烈建议、3 项锦上添花。

---

## ⭐ 12 项处置状态(2026-05-09 主 agent 复核后)

| # | 问题 | M4 现状 | 处置 |
|---|------|--------|------|
| 1 | DECIMAL 与 Stripe minor unit | StripeClientImpl `.movePointRight(2).longValueExact()` 转 cents,USD/HKD/CNY 全 2 小数无影响;**JPY 等零小数币种**白名单工具类留 backlog | ⚠️ 部分(JPY 启用前补) |
| 2 | webhook 事件清单缺 dispute/fraud/processing | 复核确认缺 6 个;新增 patch 不影响 schema,**StripeWebhookHandler dispatch 加 6 个事件** + 设计文档同步(spec/plan 改 4 → 10 个事件) | ✅ 必改并入 |
| 3 | 服务端 vs 前端金额来源 | `createCheckout(userId, packageId, code)` 只接 packageId,服务端 `package.price_usd` 计算,从设计上规避 | ✅ 已规避 |
| 4 | Stripe Customer 映射 | M4 加 `user_payment_profile` 表(20260509_180000 patch)+ StripeClient.createOrGetCustomer 方法 + PaymentService.createCheckout lazy create | ✅ 必改并入 |
| 5 | Idempotency Key | StripeClientImpl 显式 `RequestOptions.builder().setIdempotencyKey("payment-{id}" / "refund-{id}")` | ✅ 已规避 |
| 6 | 30min 超时同步 cancel Stripe PI | M4 用 Hosted Checkout `expires_at` 参数,Stripe 自动 cancel Session + PaymentIntent + 发 expired webhook,不依赖本地 Job | ✅ 已规避(比老设计更优) |
| 7 | payment_method_type 字段 | 新增 patch `ALTER TABLE payment ADD COLUMN payment_method_type VARCHAR(32)` + handleCheckoutCompleted webhook 回填 | ✅ 必改并入 |
| 8 | refund 防重 DB 强约束 | 新增 patch `is_active GENERATED + UNIQUE(payment_id, is_active)` 替代 Service 层乐观锁 | ✅ 必改并入 |
| 9 | currency VARCHAR(8) → VARCHAR(3) | 新增 patch `ALTER TABLE payment MODIFY currency_request/_paid VARCHAR(3)` | ✅ 必改并入 |
| 10 | DATETIME(3) 流水时间精度 | M4 保持 DATETIME(秒级),Stripe webhook epoch 秒级,撞秒罕见 | ➖ 设计选择保持 |
| 11 | Stripe metadata 反向写入 | StripeClientImpl `.putMetadata("payment_id" / "user_id" / "package_id")` | ✅ 已规避 |
| 12 | webhook 重试指数退避 | M4 设计:返回 200 即停 Stripe retry,失败靠运维监控 stripe_event.result='failed' + M5 admin 重放入口(不做本地 retry job) | ➖ 设计方向不同,可接受 |

**总结**:5 项必改全部并入 M4(`patch/20260509_180000_apply_review_fixes.sql` + plan Phase 1.5 Task 1.5 + Phase 2 Track 2E + Phase 3 Track 3A 增补 + Phase 4 Task 4.1 webhook 事件扩 dispatch);3 项已规避;3 项设计差异可接受;1 项(JPY)留 backlog。

---

## 一、整体评价

mandarly 这套支付设计在**架构层面**已经走在大部分开源支付系统(包括 Jeepay)前面,值得肯定的关键决策:

| 维度 | mandarly | Stripe 官方 sample | 评价 |
|---|---|---|---|
| Webhook 幂等 | `payment_webhook_event.event_id` UNIQUE | 无,直接处理 | 正确 |
| 先 ACK 再处理 | 入站验签 → 落库 → 200 → 异步 | 同步处理完才返 200 | 关键决策 |
| 防篡改 | `ref_intent` JSON Schema 强校验 + succeeded 时金额一致性比对 | hardcode `5999L` | 关键 |
| 状态分离 | webhook 事件状态机 ≠ payment 状态机 | 无 | 可追溯 |
| 退款双语义 | `refund_target_type` 区分 payment / course_order | N/A | 业务到位 |
| Webhook payload > 64KB 预案 | 已注明走 COS | N/A | 前瞻 |

下文 12 项问题,均针对**生产可用性**,不是架构问题。

---

## 二、问题清单

### 【严重】#1 `amount DECIMAL(12,2)` 与 Stripe 单位制不匹配

**现状**

`payment` 表 line 321 (`server/sql/mysql/mandarly.sql:321`):

```sql
`amount` DECIMAL(12,2) NOT NULL,
```

设计文档 §4.1 说明该字段是"实际收款金额",从上下文看是主单位(如 `299.00`)。

**Stripe 实际要求**

Stripe API 所有金额字段均以 **smallest currency unit** 表示,类型 `Long`。Stripe 官方 Java sample (`payment-refs/accept-a-payment/custom-payment-flow/server/java/src/main/java/com/stripe/sample/Server.java:108`):

```java
.setAmount(5999L);  // 5999 cents = $59.99
```

**风险**

1. 调 `PaymentIntent.create()` 时需要 `× 100` 转换,这是 bug 高发点
2. **零小数位币种(JPY / KRW / VND 等)不能 × 100**:
   - `JPY 299` → Stripe 应传 `299L`,**不是** `29900L`
   - 一旦做 JPY 业务,会出现"用户付 299 日元,实际扣 29900 日元"的事故
3. 反向问题:Webhook 回来的 amount 是 minor unit,本地存储前要除回去,容易精度丢失

**建议**

方案 A(推荐):新增 `amount_minor BIGINT NOT NULL`,与 Stripe 一一对应,DECIMAL 字段保留供报表用、由 Service 层在 succeeded 时同步写入

方案 B:维持 DECIMAL,但提供工具类 `StripeAmountUtils.toMinor(BigDecimal, currency)` / `fromMinor(Long, currency)`,内置零小数位币种白名单(JPY、KRW、VND、CLP、ISK 等),并对该工具类做单元测试覆盖所有币种

**验收点**

- [ ] 文档/代码中明确金额单位约定
- [ ] 有零小数位币种处理逻辑
- [ ] 单元测试覆盖 HKD/USD/CNY/JPY 至少 4 种币种的双向转换

---

### 【严重】#2 Webhook 事件清单遗漏跨境支付关键事件

**现状**

设计文档 §3.1 + §3.3 明确监听的事件:
- `payment_intent.succeeded`
- `payment_intent.payment_failed`
- `charge.refunded`

**问题**

跨境支付最高风险的几类事件**全部未列出**,这是从"国内支付经验"迁移到 Stripe 时最容易忽视的。

| 事件 | 重要性 | 业务含义 |
|---|---|---|
| `charge.dispute.created` | 极高 | **拒付(Chargeback)发生**。用户向发卡行投诉,资金被冻结。Stripe 给 7-21 天提交证据,逾期或败诉 → 钱被扣回 + 罚金($15) |
| `charge.dispute.closed` | 高 | 拒付结案(won/lost),决定钱是否最终扣回 |
| `radar.early_fraud_warning.created` | 高 | Stripe Radar 风控提前预警(往往领先 dispute 几小时-几天) |
| `payment_intent.canceled` | 中 | PaymentIntent 主动取消(我方 30 分钟超时 Job 触发后会收到 webhook) |
| `payment_intent.processing` | 中 | 异步支付方式中间态(ACH / SEPA / 部分本地支付) |
| `charge.refund.updated` | 中 | 退款状态变更(罕见的退款失败) |

**风险量化**

- 一笔 dispute 没及时响应 = 全额损失 + $15 罚金
- 跨境业务月均 dispute 率行业大致 0.1%-0.9%,1000 笔订单可能有 1-9 起 dispute
- Stripe 对 dispute 率 > 0.75% 的商户会发警告,> 1% 会封号

**建议**

1. 一期至少接入 `charge.dispute.created` + `charge.dispute.closed` + `radar.early_fraud_warning.created`,处理动作可以简单:**落库 + 邮件/IM 告警给运营**
2. `payment_intent.canceled` / `processing` 加入 known list,标记为 `ignored` 或落库不告警
3. 设计文档 §3.1 状态机增加 `disputed` 状态(或在 payment 表加 `dispute_status` 字段,因为 dispute 不是 payment 主状态)

**验收点**

- [ ] 文档列出完整 webhook 事件清单(至少补 6 个)
- [ ] 有 dispute 告警通道设计(邮件/IM/工单)
- [ ] 数据库或服务层有 dispute 落库结构

---

### 【严重】#3 服务端创建 payment 时金额来源未明确

**现状**

设计文档 §4.1 业务约束 5 写了 succeeded 时校验:

> `payment.amount == ref_intent.expected_amount - ref_intent.discount_amount`

但**创建 payment 那一步**(学生发起支付时),设计文档没明确说 `payment.amount` 从哪里来。

**风险**

如果 Service 层直接信任前端传来的 `expected_amount`:

1. 前端发起支付时把 `expected_amount: "299.00"` 改成 `"1.00"` → payment.amount = 1.00
2. Stripe 实际扣款 1.00 → succeeded webhook 回来 amount = 1.00
3. **业务约束 5 校验通过**(因为前后两边都是被篡改后的 1.00)
4. 系统给学生发了 299 元的套餐,但只收到 1 元

业务约束 5 防的是"前端展示和 Stripe 不一致",**不是**防"前端传给后端的就是错的"。

**建议**

设计文档 §4.1 业务约束补一条(明确写在文档里,避免不同开发各写各的):

> **业务约束 6**: Service 创建 payment 时,`payment.amount` 必须**服务端根据 `package_id` + `currency` 重新查表计算**(`amount = package.price - referral_discount`),前端传来的 `expected_amount` **仅用作展示一致性的双重校验**(若不一致 → 拒绝创建,避免前端展示价与后端计算价漂移误导用户),**绝不直接采纳前端的 amount**。

**验收点**

- [ ] 文档明确金额计算源头是服务端
- [ ] Service 实现时单元测试覆盖"前端传错 amount"的拒绝场景

---

### 【重要】#4 缺 Stripe Customer 映射

**现状**

`user` 表没有 `stripe_customer_id` 字段,设计文档也没提 customer 映射。每笔 payment 直接关联 user_id + 创建匿名 PaymentIntent。

**为什么要有**

1. **二期订阅 / 保存卡片必须有**: Stripe SetupIntent / Subscription API 都要求 Customer 对象
2. **Dispute 处理大幅简化**: Stripe Dashboard 按 customer 聚合视图,处理拒付争议时能一眼看到该用户历史交易、地址、IP 模式
3. **Radar 风控**: Stripe 风控引擎会基于 customer 历史(失败次数、地址变更频率)自动调整阈值
4. **未来扩展第二支付通道**(如 PayPal / 空中云汇),需要存多个 channel_customer_id

**建议**

新增表 `user_payment_profile`(避免 user 表加多个外部 ID 字段):

```sql
CREATE TABLE `user_payment_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `channel` VARCHAR(16) NOT NULL COMMENT 'stripe/paypal/airwallex',
  `channel_customer_id` VARCHAR(128) NOT NULL COMMENT 'cus_xxx',
  `default_payment_method` VARCHAR(128) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `tenant_id` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_channel` (`user_id`, `channel`, `deleted`)
);
```

第一次发起支付时 lazy create Stripe customer,缓存 customer_id。

**验收点**

- [ ] 设计文档增加 customer 映射子节
- [ ] 一期 schema 即包含此表(避免二期再迁移)

---

### 【重要】#5 调 Stripe API 缺 Idempotency Key

**现状**

设计文档讲了 webhook 入站幂等(`event_id` UNIQUE),但**出站调用** Stripe API 的幂等没有提及。

**问题**

- 网络抖动 / 容器重启 / 我方重试,会导致 `PaymentIntent.create()` 被调用两次 → Stripe 端真的创建两个 PaymentIntent
- `Refund.create()` 同理 → 退两次款

Stripe SDK 原生支持 Idempotency-Key header:

```java
RequestOptions options = RequestOptions.builder()
    .setIdempotencyKey("payment-create-" + payment.getId())
    .build();
PaymentIntent intent = PaymentIntent.create(params, options);
```

Stripe 服务端会缓存 24 小时内同 key 的请求结果,直接返回首次结果。

**建议**

1. 设计文档 §4.1 业务约束补:Service 调 Stripe API(create/cancel/refund)**必须**带 idempotency-key
2. Key 命名规范:
   - `payment-create-{payment.id}`
   - `payment-cancel-{payment.id}`
   - `refund-create-{refund.id}`
3. 退款 key 用 `refund.id` 不能用 `payment.id`(同一 payment 可能多次退款,如二期支持部分退)

**验收点**

- [ ] 文档说明出站幂等策略
- [ ] 代码所有调用 Stripe 写操作的地方都有 idempotency-key
- [ ] Code review checklist 增加该项

---

### 【重要】#6 30 分钟超时取消未同步取消 Stripe PaymentIntent

**现状**

设计文档 §1.6:

> `payment.status='pending'` 持续 30 分钟未变 → 定时 Job 改 `status='cancelled'`

**问题**

定时 Job 只改本地状态,**Stripe 端的 PaymentIntent 仍在 `requires_payment_method`**,默认 24 小时后才自动失效。

**风险**

1. 用户拿着旧的 client_secret 仍能完成支付 → Stripe 扣款成功 webhook 回来 → 本地是 `cancelled`,系统拒绝创建 student_package → 钱进了账但服务没给,投诉
2. 用户重新发起支付 → 同一 ref_intent 在 Stripe 端有两个 active PaymentIntent,Dashboard 难看且对账复杂

**建议**

定时 Job 改本地状态前(或同步),调:

```java
PaymentIntent.retrieve(payment.getChannelTxnId())
    .cancel(PaymentIntentCancelParams.builder()
        .setCancellationReason(PaymentIntentCancelParams.CancellationReason.ABANDONED)
        .build(),
        RequestOptions.builder().setIdempotencyKey("payment-cancel-" + payment.getId()).build());
```

注意只能 cancel 处于 `requires_payment_method` / `requires_confirmation` / `requires_action` / `processing` 状态的 intent,已 succeeded 的 cancel 会报错(此时应触发 dispute 流程不是 cancel)。

**验收点**

- [ ] 设计文档 §1.6 补充"同时调 Stripe cancel API"
- [ ] cancel 失败的兜底(intent 已 succeeded)有处理路径

---

### 【重要】#7 缺 `payment_method_type` 字段

**现状**

`payment` 表只记录了 `channel='stripe'`,没记录用户**实际使用的支付方式**(card / wechat_pay / alipay / link / acss_debit / au_becs_debit / ...)。

**为什么要有**

1. **退款时长不同**:卡 5-10 工作日,wechat_pay 即时,SEPA 1-2 天 — 客服回复学生"为啥退款没到账"找不到依据
2. **风控分析**:不同支付方式 dispute 率差异极大(card > wallet)
3. **对账分类**:财务对账按支付方式分组是基础需求
4. **报表**:运营要看"微信支付占比"

**建议**

加字段:

```sql
`payment_method_type` VARCHAR(32) DEFAULT NULL COMMENT 'card/wechat_pay/alipay/link/...',
```

填充时机:Webhook `payment_intent.succeeded` 时从 `payment_intent.payment_method` 反查 PaymentMethod 对象的 `type` 回填。

**验收点**

- [ ] schema 加字段
- [ ] webhook handler 有回填逻辑

---

### 【重要】#8 Refund 防重靠 Service 层乐观锁,DB 无强约束

**现状**

设计文档 §4.2 业务约束 1:

> 同一 `refund_target_id` 同时只能有一条 pending / approved / refunding 状态的 refund(防重复退,Service 层 + 唯一索引?这里用 Service 层乐观锁)

**问题**

文档自己用问号表示不确定。Service 层乐观锁在并发场景(管理员 A、B 同时审核)下可能漏。MySQL 不支持 partial unique index,但有替代方案:

**建议方案 A**:加 `is_active` 派生字段

```sql
ALTER TABLE `refund` ADD COLUMN `is_active` TINYINT(1) GENERATED ALWAYS AS
  (CASE WHEN status IN ('pending','approved','refunding') THEN 1 ELSE NULL END) STORED;

-- 利用 MySQL "唯一索引允许多个 NULL" 的特性
ALTER TABLE `refund` ADD UNIQUE KEY `uk_refund_target_active`
  (`refund_target_type`, `refund_target_id`, `is_active`);
```

**建议方案 B**:Redisson 分布式锁(项目脚手架应已有),在 Service 层把 `refund_target_type:refund_target_id` 作为锁 key

**验收点**

- [ ] 设计文档去掉"?"明确选定方案
- [ ] schema 或代码中有强约束实现
- [ ] 单元测试覆盖"两个管理员同时审核"

---

### 【重要】#9 `currency VARCHAR(8)` 长度过宽

**现状**

`payment.currency` / `refund.currency` / `teacher_income.currency` 等多处 `VARCHAR(8)`。

**问题**

ISO 4217 货币代码固定 3 字符。`VARCHAR(8)` 是历史遗留陋习,会带来:

1. 索引浪费空间
2. 不一致风险:有人写 `'HKD'`,有人写 `'hkd '`(尾空格)
3. 校验不够严格

**建议**

全局 `VARCHAR(3)`,Service 层强制 uppercase。低风险改动,建议在第一版投产前完成。

**验收点**

- [ ] 全表(payment / refund / teacher_income / teacher_balance / teacher_withdrawal)统一改 VARCHAR(3)
- [ ] 入参校验 length=3 + uppercase

---

### 【建议】#10 时间字段精度不统一

**现状**

- `payment.paid_at` / `expires_at` / `cancelled_at` 是 `DATETIME`(秒精度)
- `teacher_income.settle_at` / `frozen_until` 是 `DATETIME(3)`(毫秒精度)
- `teacher_balance.update_time` 是 `DATETIME(3)`

**问题**

不一致;且 Stripe webhook 时间戳是 epoch 秒,跨表对账可能"撞秒"导致排序歧义。

**建议**

涉及金额流水、对账的时间字段统一 `DATETIME(3)`(`payment.paid_at`、`refund.refunded_at` 重点)。`create_time` / `update_time` 这种审计字段保持原样即可。

---

### 【建议】#11 Stripe metadata 反向写入

**现状**

通过 `payment.channel_txn_id` 反查 Stripe → 单向。

**建议**

创建 PaymentIntent 时把本地 ID 写入 metadata:

```java
params.putAllMetadata(Map.of(
    "mandarly_payment_id", String.valueOf(payment.getId()),
    "mandarly_user_id", String.valueOf(payment.getUserId()),
    "mandarly_package_id", refIntent.getPackageId().toString()
));
```

收益:Stripe Dashboard 直接搜本地 ID,客服/运营/财务排查工单效率显著提升。

---

### 【建议】#12 Webhook 重试策略改指数退避

**现状**

设计文档 §3.3 业务约束 3:

> 失败重试:`process_attempt < 5` 时,定时 Job 重新触发处理;超 5 次进 alert

**问题**

1. 5 次 hardcode,不可配置
2. 没说间隔,如果是固定间隔(如每分钟扫一次)失败时 5 分钟内会猛轰下游

**建议**

指数退避(参考 Stripe 自身 webhook 重试策略):

| attempt | 间隔(从首次失败算) |
|---|---|
| 1 | 1 min |
| 2 | 5 min |
| 3 | 30 min |
| 4 | 2 hour |
| 5 | 12 hour |

通过 `next_retry_at` 字段实现,Job 扫描 `process_status='failed' AND next_retry_at <= NOW()`。

---

## 三、附录:复核 checklist(Agent 用)

复核 Agent 请逐项验证下列条目,在每条后标注 ✅ 已落实 / ⚠️ 部分落实 / ❌ 未落实 / ➖ 不适用:

### 严重(必改)

- [ ] **#1 amount 单位**: 文档/代码明确 Stripe minor unit 转换;有零小数位币种白名单
- [ ] **#2 Webhook 事件**: 至少补充 `charge.dispute.created` / `charge.dispute.closed` / `radar.early_fraud_warning.created` / `payment_intent.canceled` / `payment_intent.processing` / `charge.refund.updated`
- [ ] **#3 金额来源**: 文档明确 payment.amount 由服务端 `package.price - discount` 计算,不取自前端

### 重要(强烈建议)

- [ ] **#4 Customer 映射**: 有 `user_payment_profile` 表或等价方案
- [ ] **#5 Idempotency Key**: 文档要求所有 Stripe 写操作带 key
- [ ] **#6 超时同步 cancel**: 30 分钟 Job 同时调 Stripe `PaymentIntent.cancel`
- [ ] **#7 payment_method_type**: payment 表加该字段,webhook 回填
- [ ] **#8 Refund 防重**: 有 DB 强约束(非纯 Service 乐观锁)
- [ ] **#9 currency 长度**: 全部改 VARCHAR(3)

### 建议(锦上添花)

- [ ] **#10 时间精度**: 流水类字段统一 DATETIME(3)
- [ ] **#11 metadata 反向**: 创建 intent 时写入 mandarly_payment_id
- [ ] **#12 指数退避**: webhook 重试有 next_retry_at 字段

---

## 四、复核 Agent 调用建议

复核此报告时建议:

1. 对比当前 `docs/database/03-payment.md`(可能已被更新)与本报告项目逐项核对
2. 检查 `server/sql/mysql/mandarly.sql` 中 `payment` / `refund` / `payment_webhook_event` 三张表是否按本报告调整
3. 若 mandarly-module-edu 下已有 Java 实现(`controller/webhook/stripe/`、`service/payment/`),核对:
   - 是否使用 Stripe SDK 的 `Webhook.constructEvent` 验签
   - 出站调用是否带 `IdempotencyKey`
   - 金额计算是否服务端独立完成
   - Webhook handler 是否覆盖 dispute 系列事件
4. 输出复核报告:每项给 ✅/⚠️/❌/➖ 标注 + 一句话证据

---

**复核报告来源**: 本报告基于与 Stripe 官方 Java sample (`/Users/liuguanye/home/Develop/project/payment-refs/accept-a-payment/`) 对照 + 跨境支付实战经验整理,非来自 Stripe 官方安全审计,落地时建议结合 Stripe 官方文档 [Best practices](https://stripe.com/docs/payments/payment-intents/best-practices) 二次确认。
