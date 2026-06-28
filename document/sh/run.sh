#!/usr/bin/env bash
# Nova Mall 后端服务启动脚本
# 使用方式: ./run.sh [admin|portal|search|all]

set -e

APP_NAME=${1:-all}
APP_VERSION="1.0-SNAPSHOT"
GROUP_NAME="mall"
PROFILE_ACTIVE="prod"

cd "$(dirname "$0")/../../deploy"

build_and_start() {
    local service=$1
    echo "==== 构建并启动 ${service} ===="
    docker compose -f docker-compose-app.yml up -d --build ${service}
    echo "==== ${service} 启动完成 ===="
}

case $APP_NAME in
    admin)
        build_and_start mall-admin
        ;;
    portal)
        build_and_start mall-portal
        ;;
    search)
        build_and_start mall-search
        ;;
    all)
        echo "==== 构建并启动所有后端服务 ===="
        docker compose -f docker-compose-app.yml up -d --build
        echo "==== 所有服务启动完成 ===="
        echo "Admin:  http://localhost:8080/swagger-ui.html"
        echo "Portal: http://localhost:8085/swagger-ui.html"
        echo "Search: http://localhost:8081/swagger-ui.html"
        ;;
    *)
        echo "用法: ./run.sh [admin|portal|search|all]"
        exit 1
        ;;
esac
