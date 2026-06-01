# mandarly-server

Mandarly 在线中文培训平台后端,基于 [RuoYiBase](https://github.com/kklgy/ruoyibase)(干净化的 ruoyi-vue-pro 脚手架)派生,通过 ProjectReactor 改名为 `com.mandarly.boot` 命名空间。

## 模块

```
mandarly-dependencies        BOM
mandarly-framework           框架公共封装
mandarly-module-system       系统管理(用户/角色/菜单/权限,沿用)
mandarly-module-infra        基础设施(沿用)
mandarly-server              启动入口(端口 48080)
```

业务模块计划在 `mandarly-module-edu/` 下按子域组织(teacher / student / package / schedule / booking / classroom / payment / income / withdrawal / referral / review / notification),详见根 `CLAUDE.md`。

## 启动

前置:本机 MySQL (root/123456) + Redis (6379) 起好,数据库名 `mandarly`,从 `sql/mysql/ruoyi-vue-pro.sql` + `quartz.sql` 初始化。

```bash
mvn install -DskipTests
mvn spring-boot:run -pl mandarly-server -Dspring-boot.run.profiles=local
```

API 前缀:管理端 `/admin-api/`,用户端 `/app-api/`。

## 致谢

- 上游脚手架:[RuoYiBase](https://github.com/kklgy/ruoyibase)(基于 [ruoyi-vue-pro](https://github.com/YunaiV/ruoyi-vue-pro) MIT)
