#!/usr/bin/env bash
# Nova Mall Portal 后端服务管理脚本
# 使用方式: ./mall-portal.sh [start|stop|restart|logs]

set -e

APP_NAME="mall-portal"
cd "$(dirname "$0")/../../deploy"

case ${1:-start} in
    start)
        echo "==== 启动 ${APP_NAME} ===="
        docker compose -f docker-compose-app.yml up -d --build ${APP_NAME}
        echo "==== 启动完成: http://localhost:8085/swagger-ui.html ===="
        ;;
    stop)
        echo "==== 停止 ${APP_NAME} ===="
        docker compose -f docker-compose-app.yml stop ${APP_NAME}
        ;;
    restart)
        echo "==== 重启 ${APP_NAME} ===="
        docker compose -f docker-compose-app.yml restart ${APP_NAME}
        ;;
    logs)
        docker compose -f docker-compose-app.yml logs -f ${APP_NAME}
        ;;
    *)
        echo "用法: ./mall-portal.sh [start|stop|restart|logs]"
        exit 1
        ;;
esac
