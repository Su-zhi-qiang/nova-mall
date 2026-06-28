# Windows 环境搭建

## 开发工具

- **IDEA**：推荐使用 IntelliJ IDEA，安装 Lombok 插件
- **VSCode**：前端开发推荐，安装 Volar、ESLint、Prettier 插件
- **Node.js**：v20.19.0+ 或 v22.12.0+（前端构建）
- **JDK**：OpenJDK 17（后端运行）
- **Python**：3.10+（Agent 服务）

## MySQL 8.0

- 下载地址：https://dev.mysql.com/downloads/mysql/
- 安装后执行初始化脚本：`mysql -u root -p < document/init.sql`
- 验证：`mysql -u root -p -e "SHOW DATABASES;"` 应看到 `mall` 数据库

## Redis

- Windows 版下载：https://github.com/tporadowski/redis/releases
- 或使用 Docker：`docker run -d -p 6379:6379 redis:7.0`
- 启动：`redis-server.exe redis.windows.conf`

## Elasticsearch 7.17

- 下载地址：https://www.elastic.co/downloads/past-releases/elasticsearch-7-17-3
- 解压后运行 `bin\elasticsearch.bat`
- 安装 IK 中文分词插件：
  ```bash
  bin\elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.17.3/elasticsearch-analysis-ik-7.17.3.zip
  ```
- 验证：访问 `http://localhost:9200`

## MongoDB 5.0

- 下载地址：https://www.mongodb.com/try/download/community
- 安装后创建数据目录：`mkdir c:\mongodb\data\db`
- 启动：`mongod --dbpath c:\mongodb\data\db`
- 客户端工具：https://robomongo.org/download（Robo 3T）

## RabbitMQ 3.9

- 先安装 Erlang：http://erlang.org/download/
- 下载 RabbitMQ：https://www.rabbitmq.com/install-windows.html
- 启用管理插件：
  ```bash
  rabbitmq-plugins enable rabbitmq_management
  ```
- 访问管理界面：`http://127.0.0.1:15672`（guest/guest）
- 创建用户 `mall`，密码 `mall`，创建 virtual host `/mall`

## MinIO（可选）

- 下载地址：https://min.io/download
- 启动：
  ```bash
  minio.exe server D:\minio\data --console-address ":9001"
  ```
- 访问控制台：`http://localhost:9001`（minioadmin/minioadmin）

## 启动后端

```bash
cd nova-mall

# 首次打包公共模块
mvn clean install -pl nova-mall-common,nova-mall-security -am

# 启动后台管理 (8080)
mvn spring-boot:run -pl nova-mall-admin

# 启动商城前台 (8085)
mvn spring-boot:run -pl nova-mall-portal

# 启动搜索服务 (8081)
mvn spring-boot:run -pl nova-mall-search
```

## 启动前端

```bash
# 后台管理前端 (5173)
cd nova-mall-admin-web && npm install && npm run dev

# 移动端 H5 (5174)
cd nova-mall-app-web && npm install && npm run dev:h5
```

## 启动 Agent

```bash
cd nova-mall-agent
pip install -r requirements.txt
cp .env.example .env  # 填入 DashScope API Key
python -m app.main    # 8090
```

## API 文档地址

| 服务 | 地址 |
|------|------|
| Admin | http://localhost:8080/swagger-ui.html |
| Portal | http://localhost:8085/swagger-ui.html |
| Search | http://localhost:8081/swagger-ui.html |
| Agent | http://localhost:8090/docs |
