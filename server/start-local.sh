#!/usr/bin/env bash
# Mandarly 本机启动脚本 — local profile
#
# 解决 mvn 命令行启动 spring-boot:run 时读不到 .env.local 的问题:
# 项目使用占位符 ${ENV_VAR} 引用敏感配置(GOOGLE_OAUTH_*, SMTP_PASSWORD, SMS_*, STRIPE_*),
# 而 mvn 默认不加载 .env 文件,导致占位符解析失败 / 凭证为空。
# 本脚本用 set -a / source 把 .env.local 里的变量导出到当前 shell 环境,再启 mvn。
#
# 用法:
#   cd server && ./start-local.sh                       # 默认 local profile
#   cd server && ./start-local.sh -Dspring-boot.run.profiles=dev
#   ./server/start-local.sh                             # 也可从项目根直接调
#
# 等价 IDE 配置:IDEA / VSCode 装 EnvFile 插件,指 .env.local;Run Configuration 走 IDE 内
#
# 退出码:透传 mvn 退出码

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/.env.local"

if [ ! -f "$ENV_FILE" ]; then
  echo "❌ 缺失 $ENV_FILE"
  echo "   参考 .env.example 创建,或从 your local secret manager恢复(your local secret manager)"
  exit 1
fi

echo "→ 加载 $ENV_FILE"
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

# 默认 local profile,允许通过 $1+ 覆盖或追加 mvn 参数
EXTRA_ARGS=("$@")
if [ ${#EXTRA_ARGS[@]} -eq 0 ]; then
  EXTRA_ARGS=(-Dspring-boot.run.profiles=local)
fi

echo "→ 启动 mandarly-server(profile=local,工作目录 $SCRIPT_DIR)"
cd "$SCRIPT_DIR"
exec mvn spring-boot:run -pl mandarly-server "${EXTRA_ARGS[@]}"
