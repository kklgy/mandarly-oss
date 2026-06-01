# Mandarly App (用户端)

学生 / 教师 Web 端,响应式 PC + H5 同源。

## 技术栈
- Vue 3.5 + Vite 6
- Pinia + Vue Router 4
- Element Plus(PC 组件) + Vant(移动端组件)
- vue-i18n(简中 + 繁中 + 英文)
- 通过 `/app-api/` 前缀访问后端

## 启动
```bash
pnpm install   # 或 npm install
pnpm dev       # 默认 http://localhost:3001
```

## 构建
```bash
pnpm build
```

## 目录约定
```
src/
├── views/         按业务页面分目录(home/teacher/booking/classroom/package/user)
├── components/    通用组件
├── router/        Vue Router
├── stores/        Pinia store
├── api/           axios 封装与接口定义
├── i18n/          vue-i18n 字典
├── layouts/       布局组件
└── assets/        静态资源 + 全局样式
```
