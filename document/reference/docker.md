# Docker 部署参考

## 常用命令

### 镜像操作

```bash
# 构建镜像
docker build -t mall/mall-admin:1.0 .

# 查看镜像
docker images

# 删除镜像
docker rmi mall/mall-admin:1.0
```

### 容器操作

```bash
# 后台运行
docker run -d -p 8080:8080 --name mall-admin mall/mall-admin:1.0

# 查看运行中的容器
docker ps

# 查看所有容器
docker ps -a

# 停止容器
docker stop mall-admin

# 启动已停止的容器
docker start mall-admin

# 进入容器
docker exec -it mall-admin /bin/bash

# 查看日志
docker logs -f mall-admin

# 删除容器
docker rm mall-admin
```

### Docker Compose

```bash
# 启动所有服务
docker compose -f docker-compose-app.yml up -d --build

# 启动指定服务
docker compose -f docker-compose-app.yml up -d mall-admin

# 查看服务状态
docker compose -f docker-compose-app.yml ps

# 停止服务
docker compose -f docker-compose-app.yml stop

# 查看日志
docker compose -f docker-compose-app.yml logs -f mall-admin
```

## 本项目部署

### 中间件部署

```bash
cd nova-mall/document/docker
docker compose -f docker-compose-env.yml up -d
```

包含：MySQL 8.0、Redis 7.0、MongoDB 5.0、RabbitMQ 3.9、Elasticsearch 7.17、Nginx 1.22

### 后端服务部署

```bash
cd nova-mall/deploy
docker compose -f docker-compose-app.yml up -d --build
```

包含：mall-admin(:8080)、mall-portal(:8085)、mall-search(:8081)

### Agent 部署

```bash
cd nova-mall-agent
docker build -t mall-agent:1.0 .
docker run -d -p 8090:8090 --name mall-agent mall-agent:1.0
```

## 资源限制

| 组件 | 内存限制 |
|------|---------|
| MySQL | 1024M |
| Redis | 80M |
| MongoDB | 256M |
| RabbitMQ | 200M |
| Elasticsearch | 400M |
| mall-admin | 450M |
| mall-portal | 450M |
| mall-search | 400M |
| mall-agent | 512M |
| Nginx | 64M |

## 注意事项

- MySQL 必须使用 `mysql:8.0.35`，不能用 `mysql:8.0`
- MySQL 必须配置 `bind-address=0.0.0.0`
- 导入 SQL 后执行 `GRANT ALL PRIVILEGES ON mall.* TO 'root'@'%'; FLUSH PRIVILEGES;`
- 2核4G 服务器必须配置 4GB Swap
- ES 堆内存 `ES_JAVA_OPTS=-Xmx128m` 需小于容器限制
