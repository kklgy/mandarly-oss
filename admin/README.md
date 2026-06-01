# mandarly-admin

Mandarly 在线中文培训平台管理端,基于 [yudao-ui-admin-vben](https://github.com/yudaocode/yudao-ui-admin-vben) (vben monorepo v5.7.0) vendor 而来,通过 [RuoYiBase](https://github.com/kklgy/ruoyibase) 派生时一并接管。

## 结构

vben 是 monorepo,默认使用 `apps/web-antd`(Ant Design Vue 4):

```
apps/
  web-antd/        默认入口
  web-antdv-next/  备选
  web-ele/         备选(Element Plus)
  web-naive/       备选(Naive UI)
  web-tdesign/     备选(TDesign)
packages/          共享 UI 组件 / hooks / preferences
internal/          构建工具
```

## 启动

```bash
pnpm install
pnpm dev:antd    # → http://localhost:5666
```

后端默认指向 `http://localhost:48080`,在 `apps/web-antd/.env*` 内配置。

## 致谢

- 上游 admin:[yudao-ui-admin-vben](https://github.com/yudaocode/yudao-ui-admin-vben) (MIT)
- 中间脚手架:[RuoYiBase](https://github.com/kklgy/ruoyibase)
