# Nova Mall 项目部署方案（完整版 v10.0）

## 项目概述

| 层级   | 技术栈                                                       |
| ------ | ------------------------------------------------------------ |
| 后端   | Spring Boot 3.5.14 + Java 17（mall-admin :8080 / mall-portal :8085 / mall-search :8081） |
| Agent  | Python 3.11 + FastAPI + LangChain + ChromaDB（:8000）        |
| 前端   | Vue 3 + Vite + Element Plus（管理后台）/ uni-app（移动端H5） |
| 中间件 | MySQL 8.0.35、Redis 7.0、MongoDB 5.0、RabbitMQ 3.9、Elasticsearch 7.17.3、Nginx 1.22 |
| 存储   | 阿里云 OSS                                                   |

---

## 一、服务器配置

| 配置项   | 内容              |
| -------- | ----------------- |
| 公网 IP  | <YOUR_SERVER_IP> |
| 实例规格 | 2核4G             |
| 系统盘   | ESSD Entry 40GB   |
| 镜像     | Ubuntu 22.04 64位 |

### 安全组入方向

| 协议 | 端口 | 说明        |
| ---- | ---- | ----------- |
| TCP  | 22   | SSH         |
| TCP  | 80   | HTTP        |
| TCP  | 443  | HTTPS       |
| TCP  | 3306 | MySQL       |
| TCP  | 8080 | mall-admin  |
| TCP  | 8081 | mall-search |
| TCP  | 8085 | mall-portal |

---

## 二、环境初始化

```bash
# 系统更新
apt update && apt upgrade -y

# 安装 Docker
apt install docker.io -y
systemctl start docker && systemctl enable docker

# 安装 Docker Compose
apt install docker-compose-plugin -y

# 镜像加速
cat > /etc/docker/daemon.json << 'EOF'
{
  "dns": ["223.5.5.5", "114.114.114.114"],
  "registry-mirrors": [
    "https://docker.xuanyuan.me",
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://hub-mirror.c.163.com"
  ]
}
EOF
systemctl daemon-reload && systemctl restart docker

# ⚠️ 关键：配置 Swap（2核4G 服务器必须配置）
dd if=/dev/zero of=/swapfile bs=1M count=4096
chmod 600 /swapfile
mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

# 验证 Swap
swapon --show
free -h

# 目录结构
mkdir -p /opt/mall/{docker/app,data/{mysql,redis,mongodb,rabbitmq,elasticsearch,nginx-html,agent/{chroma_db,knowledge_base}},logs/{mall-admin,mall-portal,mall-search,mall-agent,nginx},config/{mysql/conf.d,nginx/conf.d}}
cd /opt/mall/docker
```

---

## 三、中间件部署

### 3.1 MySQL 配置（⚠️ 关键：必须包含 bind-address）

```bash
cat > /opt/mall/config/mysql/conf.d/my.cnf << 'EOF'
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default-time-zone='+08:00'
lower_case_table_names=1
max_connections=100
innodb_buffer_pool_size=128M
innodb_log_buffer_size=16M
innodb_flush_log_at_trx_commit=2
innodb_io_capacity=200
innodb_io_capacity_max=400
bind-address=0.0.0.0

[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4
EOF
```

### 3.2 docker-compose-env.yml

```bash
cat > /opt/mall/docker/docker-compose-env.yml << 'EOF'
services:
  mysql:
    image: mysql:8.0.35
    container_name: mall-mysql
    restart: always
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: <YOUR_MYSQL_PASSWORD>
      MYSQL_DATABASE: mall
      TZ: Asia/Shanghai
    command:
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --innodb-buffer-pool-size=128M
      --max-connections=100
      --bind-address=0.0.0.0
    volumes:
      - /opt/mall/data/mysql:/var/lib/mysql
      - /opt/mall/config/mysql/conf.d:/etc/mysql/conf.d
    deploy:
      resources:
        limits:
          memory: 1024M
    networks:
      - mall-network

  redis:
    image: redis:7.0
    container_name: mall-redis
    restart: always
    ports:
      - "6379:6379"
    command: redis-server --requirepass <YOUR_REDIS_PASSWORD> --appendonly yes --maxmemory 64mb --maxmemory-policy allkeys-lru
    volumes:
      - /opt/mall/data/redis:/data
    deploy:
      resources:
        limits:
          memory: 80M
    networks:
      - mall-network

  mongodb:
    image: mongo:5.0
    container_name: mall-mongo
    restart: always
    ports:
      - "27017:27017"
    command: --wiredTigerCacheSizeGB 0.25 --maxConns 50
    volumes:
      - /opt/mall/data/mongodb:/data/db
    deploy:
      resources:
        limits:
          memory: 256M
    networks:
      - mall-network

  rabbitmq:
    image: rabbitmq:3.9-management
    container_name: mall-rabbitmq
    restart: always
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: mall
      RABBITMQ_DEFAULT_PASS: <YOUR_RABBITMQ_PASSWORD>
      RABBITMQ_DEFAULT_VHOST: /mall
      TZ: Asia/Shanghai
    volumes:
      - /opt/mall/data/rabbitmq:/var/lib/rabbitmq
    deploy:
      resources:
        limits:
          memory: 200M
    networks:
      - mall-network

  elasticsearch:
    image: elasticsearch:7.17.3
    container_name: mall-es
    restart: always
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      TZ: Asia/Shanghai
      cluster.name: mall-es
      discovery.type: single-node
      ES_JAVA_OPTS: "-Xms128m -Xmx128m"
      xpack.security.enabled: "false"
      xpack.ml.enabled: "false"
      xpack.monitoring.enabled: "false"
    volumes:
      - /opt/mall/data/elasticsearch:/usr/share/elasticsearch/data
      - /opt/mall/data/elasticsearch-plugins:/usr/share/elasticsearch/plugins
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65535
        hard: 65535
    deploy:
      resources:
        limits:
          memory: 400M
    networks:
      - mall-network

  nginx:
    image: nginx:1.22
    container_name: mall-nginx
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /opt/mall/config/nginx/conf.d:/etc/nginx/conf.d:ro
      - /opt/mall/data/nginx-html:/usr/share/nginx/html
      - /opt/mall/logs/nginx:/var/log/nginx
    deploy:
      resources:
        limits:
          memory: 64M
    networks:
      - mall-network

networks:
  mall-network:
    name: mall-network
    driver: bridge
EOF
```

### 3.3 Nginx 配置

```bash
cat > /opt/mall/config/nginx/conf.d/mall.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    resolver 127.0.0.11 valid=10s;

    # 超时配置
    proxy_connect_timeout 60s;
    proxy_send_timeout 60s;
    proxy_read_timeout 120s;
    send_timeout 60s;

    location /admin {
        alias /usr/share/nginx/html/admin/;
        try_files $uri $uri/ /admin/index.html;
    }

    location /app {
        alias /usr/share/nginx/html/app/;
        try_files $uri $uri/ /app/index.html;
    }

    location /mall-admin/ {
        set $backend "mall-admin:8080";
        rewrite ^/mall-admin/(.*)$ /$1 break;
        proxy_pass http://$backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /mall-portal/ {
        set $backend "mall-portal:8085";
        rewrite ^/mall-portal/(.*)$ /$1 break;
        proxy_pass http://$backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /mall-search/ {
        set $backend "mall-search:8081";
        rewrite ^/mall-search/(.*)$ /$1 break;
        proxy_pass http://$backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /mall-agent/agent/ {
        set $backend "mall-admin:8080";
        rewrite ^/mall-agent/(.*)$ /$1 break;
        proxy_pass http://$backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /mall-agent/ {
        set $backend "mall-agent:8000";
        rewrite ^/mall-agent/(.*)$ /$1 break;
        proxy_pass http://$backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 600s;
    }

    location / {
        return 302 /admin;
    }
}
EOF
```

### 3.4 启动中间件

```bash
cd /opt/mall/docker
docker compose -f docker-compose-env.yml up -d
sleep 60
docker ps
```

---

## 四、导入数据库

### 4.1 导入 SQL 文件

```bash
# 导入完整 SQL 文件
docker exec -i mall-mysql mysql -uroot -p<YOUR_MYSQL_PASSWORD> --default-character-set=utf8mb4 mall < /opt/mall/config/mall_full.sql

# 验证（应输出 78）
docker exec -i mall-mysql mysql -uroot -p<YOUR_MYSQL_PASSWORD> mall -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='mall';"
```

### 4.2 ⚠️ 关键：修复 MySQL 权限

```bash
# 授予 root@'%' 对 mall 数据库的完整权限
docker exec -it mall-mysql mysql -uroot -p<YOUR_MYSQL_PASSWORD> -e "GRANT ALL PRIVILEGES ON mall.* TO 'root'@'%'; FLUSH PRIVILEGES;"

# 验证权限
docker exec -it mall-mysql mysql -uroot -p<YOUR_MYSQL_PASSWORD> -e "SHOW GRANTS FOR 'root'@'%';"
```

### 4.3 添加数据库索引（性能优化）

```bash
docker exec -it mall-mysql mysql -uroot -p<YOUR_MYSQL_PASSWORD> mall << 'EOF'
-- 商品表索引
CREATE INDEX idx_product_category_id ON pms_product(product_category_id);
CREATE INDEX idx_brand_id ON pms_product(brand_id);
CREATE INDEX idx_publish_status ON pms_product(publish_status);
CREATE INDEX idx_verify_status ON pms_product(verify_status);
CREATE INDEX idx_product_sn ON pms_product(product_sn);

-- 订单表索引
CREATE INDEX idx_order_member_id ON oms_order(member_id);
CREATE INDEX idx_order_status ON oms_order(status);
CREATE INDEX idx_order_create_time ON oms_order(create_time);

-- 优惠券表索引
CREATE INDEX idx_coupon_type ON sms_coupon(type);
CREATE INDEX idx_coupon_use_type ON sms_coupon(use_type);
CREATE INDEX idx_coupon_start_time ON sms_coupon(start_time);
CREATE INDEX idx_coupon_end_time ON sms_coupon(end_time);
EOF
```

---

## 五、后端部署

### 5.1 上传 jar 包

将以下三个 jar 包上传到 `/opt/mall/docker/app/`：
- `nova-mall-admin-1.0-SNAPSHOT.jar`
- `nova-mall-portal-1.0-SNAPSHOT.jar`
- `nova-mall-search-1.0-SNAPSHOT.jar`

### 5.2 源码配置（打包前修改）

**application-prod.yml 数据库配置（三个模块）：**
```yaml
spring:
  datasource:
    url: jdbc:mysql://mall-mysql:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: <YOUR_MYSQL_PASSWORD>
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
```

**Agent llm.py timeout 修复：**
```python
DashScopeLLM._sync_client = openai.OpenAI(api_key=self.api_key, base_url=self.base_url, timeout=120.0)
DashScopeLLM._async_client = openai.AsyncOpenAI(api_key=self.api_key, base_url=self.base_url, timeout=120.0)
```

**Agent .env Redis 配置：**
```env
REDIS_HOST=mall-redis
REDIS_PORT=6379
REDIS_PASSWORD=<YOUR_REDIS_PASSWORD>
REDIS_DB=1
```

**前端 .env.production：**

管理后台：
```env
VITE_BASE_SERVER_URL = /mall-admin
VITE_AGENT_BASE_URL = /mall-agent
```

移动端：
```env
VITE_API_BASE_URL = /mall-portal
VITE_AGENT_BASE_URL = /mall-agent
```

### 5.3 创建 Dockerfile

```bash
# Dockerfile-admin
cat > /opt/mall/docker/Dockerfile-admin << 'EOF'
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY app/nova-mall-admin*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx400m -XX:+UseG1GC -XX:+UseStringDeduplication"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Dspring.profiles.active=prod app.jar"]
EOF

# Dockerfile-portal
cat > /opt/mall/docker/Dockerfile-portal << 'EOF'
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY app/nova-mall-portal*.jar app.jar
EXPOSE 8085
ENV JAVA_OPTS="-Xms256m -Xmx400m -XX:+UseG1GC -XX:+UseStringDeduplication"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Dspring.profiles.active=prod app.jar"]
EOF

# Dockerfile-search
cat > /opt/mall/docker/Dockerfile-search << 'EOF'
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY app/nova-mall-search*.jar app.jar
EXPOSE 8081
ENV JAVA_OPTS="-Xms256m -Xmx400m -XX:+UseG1GC -XX:+UseStringDeduplication"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Dspring.profiles.active=prod app.jar"]
EOF
```

### 5.4 docker-compose-app.yml

```bash
cat > /opt/mall/docker/docker-compose-app.yml << 'EOF'
services:
  mall-admin:
    build:
      context: .
      dockerfile: Dockerfile-admin
    container_name: mall-admin
    restart: always
    ports:
      - "8080:8080"
    volumes:
      - /opt/mall/logs/mall-admin:/app/logs
      - /etc/localtime:/etc/localtime:ro
    environment:
      TZ: Asia/Shanghai
    deploy:
      resources:
        limits:
          memory: 450M
    networks:
      - mall-network

  mall-portal:
    build:
      context: .
      dockerfile: Dockerfile-portal
    container_name: mall-portal
    restart: always
    ports:
      - "8085:8085"
    volumes:
      - /opt/mall/logs/mall-portal:/app/logs
      - /etc/localtime:/etc/localtime:ro
    environment:
      TZ: Asia/Shanghai
    deploy:
      resources:
        limits:
          memory: 450M
    networks:
      - mall-network

  mall-search:
    build:
      context: .
      dockerfile: Dockerfile-search
    container_name: mall-search
    restart: always
    ports:
      - "8081:8081"
    volumes:
      - /opt/mall/logs/mall-search:/app/logs
      - /etc/localtime:/etc/localtime:ro
    environment:
      TZ: Asia/Shanghai
    deploy:
      resources:
        limits:
          memory: 400M
    networks:
      - mall-network

networks:
  mall-network:
    name: mall-network
    external: true
EOF
```

### 5.5 启动后端

```bash
cd /opt/mall/docker
docker compose -f docker-compose-app.yml up -d --build
sleep 90
docker ps
```

---

## 六、Agent 智能客服

### 6.1 上传代码

将 `nova-mall-agent` 文件夹上传到 `/opt/mall/docker/`

### 6.2 Agent Dockerfile

```bash
cat > /opt/mall/docker/nova-mall-agent/Dockerfile << 'EOF'
FROM python:3.11-slim
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends build-essential && rm -rf /var/lib/apt/lists/*
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
COPY . .
RUN mkdir -p /app/chroma_db /app/knowledge_base /app/logs
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
EOF
```

### 6.3 docker-compose-agent.yml

⚠️ **重要**：知识库挂载路径必须指向源码目录，而非数据目录

```bash
cat > /opt/mall/docker/docker-compose-agent.yml << 'EOF'
services:
  mall-agent:
    build:
      context: ./nova-mall-agent
      dockerfile: Dockerfile
    container_name: mall-agent
    restart: always
    ports:
      - "8000:8000"
    volumes:
      - /opt/mall/data/agent/chroma_db:/app/chroma_db
      # ⚠️ 关键：挂载源码知识库目录
      - /opt/mall/docker/nova-mall-agent/knowledge_base:/app/knowledge_base
      - /opt/mall/logs/mall-agent:/app/logs
    env_file:
      - ./nova-mall-agent/.env
    deploy:
      resources:
        limits:
          memory: 512M
    networks:
      - mall-network
networks:
  mall-network:
    name: mall-network
    external: true
EOF
```

### 6.4 验证知识库内容

```bash
# 检查源码知识库
ls -la /opt/mall/docker/nova-mall-agent/knowledge_base/admin_knowledge/

# 如果知识库为空，手动加载
docker exec -it mall-agent python3 << 'EOF'
import sys
sys.path.append('/app')
from app.knowledge.vector_store import VectorStoreManager

store = VectorStoreManager()
store.ingest_documents('/app/knowledge_base')
store._init_store()
print(f"✅ 文档数: {len(store.collection.get()['ids'])}")
EOF
```

### 6.5 启动 Agent

```bash
cd /opt/mall/docker
docker compose -f docker-compose-agent.yml up -d --build
sleep 15
docker logs mall-agent --tail 30 | grep -i "知识库\|文档数"
```

---

## 七、前端部署

### 7.1 上传静态文件

⚠️ 上传 `dist` 目录内部的文件，不是 `dist` 文件夹本身

```bash
# 清空
rm -rf /opt/mall/data/nginx-html/admin/*
rm -rf /opt/mall/data/nginx-html/app/*

# 上传后确认有 index.html
ls /opt/mall/data/nginx-html/admin/
ls /opt/mall/data/nginx-html/app/
```

### 7.2 重启 Nginx

```bash
docker restart mall-nginx
```

---

## 八、OSS 跨域配置

登录阿里云 OSS 控制台，进入 Bucket：

1. 权限管理 → 读写权限 → 设为**公共读**
2. 数据安全 → 跨域设置 → 创建规则：
   - 来源：`http://<YOUR_SERVER_IP>`
   - 允许 Methods：`GET, POST, PUT, DELETE, HEAD`
   - 允许 Headers：`*`
   - 暴露 Headers：`ETag, x-oss-request-id, x-oss-version-id`
   - 缓存时间：`0`

---

## 九、IK 分词器

```bash
cd /opt/mall/data/elasticsearch-plugins/
wget https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.17.3/elasticsearch-analysis-ik-7.17.3.zip
unzip -o elasticsearch-analysis-ik-7.17.3.zip -d /opt/mall/data/elasticsearch-plugins/ik/
chown -R 1000:1000 /opt/mall/data/elasticsearch-plugins/
chmod -R 755 /opt/mall/data/elasticsearch-plugins/
docker restart mall-es
```

---

## 十、验证测试

```bash
# 容器状态
docker ps

# 后端
curl -I http://localhost:8080/swagger-ui/index.html
curl -I http://localhost:8081/swagger-ui/index.html
curl -I http://localhost:8085/swagger-ui/index.html

# 前端
curl -I http://localhost/admin/
curl -I http://localhost/app/

# Agent
curl http://localhost:8000/docs

# 测试登录
curl -X POST http://localhost:8080/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<YOUR_ADMIN_PASSWORD>"}'
```

---

## 十一、访问地址

| 服务       | 地址                                                  |
| ---------- | ----------------------------------------------------- |
| 管理后台   | http://<YOUR_SERVER_IP>/admin                             |
| 移动端H5   | http://<YOUR_SERVER_IP>/app                               |
| Admin API  | http://<YOUR_SERVER_IP>/mall-admin/swagger-ui/index.html  |
| Portal API | http://<YOUR_SERVER_IP>/mall-portal/swagger-ui/index.html |
| Search API | http://<YOUR_SERVER_IP>/mall-search/swagger-ui/index.html |
| Agent      | http://<YOUR_SERVER_IP>/mall-agent/docs                   |
| RabbitMQ   | http://<YOUR_SERVER_IP>:15672                             |

---

## 十二、内存分配（v10.0 优化版）

| 组件        | 限制      | 说明                        |
| ----------- | --------- | --------------------------- |
| MySQL       | 1024M     | 核心数据库，保证性能        |
| Redis       | 80M       | 缓存服务                    |
| MongoDB     | 256M      | 文档数据库                  |
| RabbitMQ    | 200M      | 消息队列                    |
| ES          | 400M      | 搜索引擎（堆内存 128M）     |
| mall-admin  | 450M      | 管理后台（堆内存 256-400M） |
| mall-portal | 450M      | 移动端（堆内存 256-400M）   |
| mall-search | 400M      | 搜索服务（堆内存 256-350M） |
| mall-agent  | 512M      | AI 客服                     |
| Nginx       | 64M       | 反向代理                    |
| **合计**    | **~3.8G** | 配合 4GB Swap 运行          |

---

## 十三、运维命令

```bash
# 状态
docker ps && docker stats --no-stream

# 日志
docker logs -f mall-admin
docker logs -f mall-portal
docker logs -f mall-search
docker logs -f mall-nginx
docker logs -f mall-agent

# 重启
cd /opt/mall/docker
docker compose -f docker-compose-env.yml restart
docker compose -f docker-compose-app.yml restart
docker compose -f docker-compose-agent.yml restart

# 清理系统缓存
sync && echo 3 > /proc/sys/vm/drop_caches

# 查看 Swap
swapon --show
free -h

# 查看系统负载
uptime
```

---

## 十四、关键注意事项

### 基础配置
1. **MySQL 镜像**：必须用 `mysql:8.0.35`，不能用 `mysql:8.0`
2. **MySQL bind-address**：必须设置 `bind-address=0.0.0.0`
3. **MySQL 权限**：导入 SQL 后执行 `GRANT ALL PRIVILEGES ON mall.* TO 'root'@'%'`
4. **MySQL 连接数**：`max_connections=100`，配合 HikariCP 最大连接数 20
5. **MySQL 内存**：1024M + `innodb_buffer_pool_size=128M`

### Swap 配置（2核4G 服务器必须）
6. **Swap 大小**：建议 4GB，防止 OOM
7. **Swap 配置**：`dd if=/dev/zero of=/swapfile bs=1M count=4096`

### 中间件配置
8. **ES 插件目录**：必须挂载 `elasticsearch-plugins`
9. **ES 内存**：`ES_JAVA_OPTS=-Xmx128m`，小于容器限制
10. **Dockerfile**：三个后端服务用三个独立 Dockerfile

### 数据库连接池
11. **HikariCP**：`maximum-pool-size: 20`，`minimum-idle: 5`
12. **数据库 URL**：加 `allowPublicKeyRetrieval=true`
13. **数据库索引**：生产环境必须添加索引

### Nginx 配置
14. **resolver**：必须加 `resolver 127.0.0.11`
15. **路径剥离**：`proxy_pass` 使用变量时配合 `rewrite`
16. **WebSocket**：必须加 `proxy_http_version 1.1` + `Upgrade`
17. **超时配置**：`proxy_read_timeout 120s`

### Agent 知识库
18. **知识库挂载**：挂载源码目录，而非数据目录
19. **知识库加载**：启动后确认文档数 > 0

---

## 十五、性能优化建议

### 15.1 服务器优化
```bash
# 配置 4GB Swap（必须）
dd if=/dev/zero of=/swapfile bs=1M count=4096
chmod 600 /swapfile
mkswap /swapfile && swapon /swapfile

# 清理系统缓存
sync && echo 3 > /proc/sys/vm/drop_caches
```

### 15.2 数据库优化
```sql
-- 添加索引（见 4.3 节）
-- 查看慢查询
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;
```

### 15.3 监控命令
```bash
# 查看负载
uptime

# 查看容器资源
docker stats --no-stream

# 查看 MySQL 连接数
docker exec -it mall-mysql mysql -uroot -p<YOUR_MYSQL_PASSWORD> -e "SHOW STATUS LIKE 'Threads_connected';"
```

---

## 十六、常见问题排查

### 16.1 MySQL 连接超时
- 检查 `bind-address=0.0.0.0`
- 检查容器网络

### 16.2 INSERT 被拒绝
```bash
docker exec -it mall-mysql mysql -uroot -p<YOUR_MYSQL_PASSWORD> -e "GRANT ALL PRIVILEGES ON mall.* TO 'root'@'%'; FLUSH PRIVILEGES;"
```

### 16.3 连接池耗尽（系统卡顿）
- 增加 HikariCP 连接池大小到 20
- 增加 MySQL `max_connections` 到 100
- 添加数据库索引

### 16.4 系统负载过高
- 检查 Swap：`swapon --show`
- 检查内存：`free -h`
- 检查容器：`docker stats --no-stream`

### 16.5 Agent 知识库为空
- 检查挂载路径是否正确
- 手动加载知识库

---

## 十七、部署检查清单

- [ ] 所有容器状态为 `Up`
- [ ] MySQL `bind-address=0.0.0.0` 已配置
- [ ] MySQL `root@'%'` 拥有完整权限
- [ ] MySQL `max_connections=100`
- [ ] Swap 4GB 已配置
- [ ] 数据库索引已添加
- [ ] ES IK 分词器已安装
- [ ] HikariCP 连接池大小已配置（20）
- [ ] 后端 API 返回 HTTP 200
- [ ] 前端页面可访问
- [ ] 默认账号可登录
- [ ] Agent 知识库文档数 > 0
- [ ] 系统负载正常（< 5）

---

## 十八、版本历史

| 版本  | 日期       | 更新内容                           |
| ----- | ---------- | ---------------------------------- |
| v6.0  | 2026-06-25 | 初始版本                           |
| v6.1  | 2026-06-26 | 修复 MySQL 权限、网络问题          |
| v7.0  | 2026-06-26 | 新增常见问题排查                   |
| v8.0  | 2026-06-26 | 新增连接池优化、性能调优           |
| v9.0  | 2026-06-26 | 修复 Agent 知识库挂载              |
| v10.0 | 2026-06-26 | 新增 Swap 配置、内存优化、性能监控 |

---

**文档版本：v10.0**

**最后更新：2026-06-26**

**本次更新内容**：
- 新增 Swap 4GB 配置（解决 OOM 问题）
- 优化 MySQL 内存限制到 1024M
- 新增 MySQL 性能优化参数
- 新增系统负载监控命令
- 新增性能优化章节
- 更新内存分配表



