# Nova Mall 后端服务

基于 Spring Boot 3.5 + MyBatis-Plus + Spring Security 的电商后端，Maven 多模块工程，包含后台管理、商城前台、全文搜索三个独立服务。

---

## 技术架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端层 (Frontend)                        │
├─────────────────────────┬───────────────────────────────────────┤
│  Admin Web (Vue3)       │  App Web (uni-app)                   │
└───────────┬─────────────┴───────────────┬───────────────────────┘
            │                             │
            ▼                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        服务层 (Backend)                         │
├───────────────┬───────────────┬───────────────┬─────────────────┤
│  Admin (8080) │ Portal (8085) │ Search (8081) │ Agent (8000)   │
│  后台管理API  │ 商城前台API   │ ES搜索服务    │ AI智能客服     │
├───────────────┴───────────────┴───────────────┴─────────────────┤
│  Security (JWT + RBAC动态权限)    Common (公共组件)              │
└─────────────────────────────────────────────────────────────────┘
            │              │              │              │
            ▼              ▼              ▼              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        中间件层 (Infra)                         │
├───────────────┬───────────────┬───────────────┬─────────────────┤
│   MySQL 8.x   │   Redis 6.x+  │  RabbitMQ 3.x │ ES 7.17       │
│   (数据存储)   │  (缓存/预减)   │  (延迟队列)    │  (全文搜索)    │
└───────────────┴───────────────┴───────────────┴─────────────────┘
```

---

## 业务架构

  <img src="document/resource/architecture.png" width="100%" alt="业务架构图">

---

## 技术亮点

- **架构设计**：工厂+策略、责任链三类设计模式，订单核心逻辑拆分为 24 个独立 Handler（下单 13 + 支付 6 + 取消 5），新增业务无需修改原有代码
- **高并发秒杀**：搭建 Redis Lua 预减库存 + MySQL 原子 SQL 兜底 + RabbitMQ 异步削峰三层防超卖架构；实现日库存快照、用户限购、库存自动回滚；通过 MQ 延迟队列自动关闭超时订单并恢复库存
- **优惠券系统**：支持 3 种适用范围、2 种优惠类型，工厂+策略模式管理
- **权限缓存**：Spring Security + JWT 动态鉴权，商品接口缓存命中率 95%+、响应 <50ms
- **AI Agent 对接**：为 AI 智能客服提供 29 个 REST API 工具（客服 23 + 后台 6），兼顾数据安全与智能化支撑

---

## 核心技术难点攻克

### 难点 1：秒杀超卖与性能瓶颈

**问题**：传统数据库扣减库存 QPS 上限低（约 200），高并发下易超卖，用户体验差。

**解决方案**：
- 采用 **Redis Lua 脚本**实现库存原子预减，毫秒级拦截超卖请求
- **MySQL 行锁**兜底扣减，保证数据最终一致性
- **RabbitMQ 延迟队列**（TTL + 死信队列）自动关闭超时订单并恢复库存
- 实现**秒杀每日库存快照**（@Scheduled + INSERT IGNORE 幂等），防止重复生成

**结果**：采用 Redis Lua 脚本原子预减 + MySQL 行锁兜底的三层防超卖架构，有效防止高并发场景下的库存超卖问题；延迟队列自动关闭超时订单并恢复库存，保障数据一致性。

### 难点 2：订单生成方法过长，难以维护

**问题**：`generateOrder()` 方法长达 **186 行**，包含 13 个顺序步骤，修改一处可能影响全局，单元测试困难。

**解决方案**：
- 采用**责任链模式**，将每个步骤拆分为独立的 Handler（平均 15-20 行/个）
- 通过 `OrderCreationChain` 串联执行，上下文 `OrderHandlerContext` 传递参数
- 新增处理步骤只需继承 `OrderHandler` 并添加到链中，**符合开闭原则**

**结果**：186 行大方法拆分为 **24 个独立 Handler**（下单 13 + 支付 6 + 取消 5），每个 Handler 独立可测试，代码可读性和可维护性大幅提升。

### 难点 3：优惠券适用范围扩展困难

**问题**：原代码中优惠券适用范围（全场/分类/商品）使用 if-else 分支判断，新增类型需要修改多处代码，违反开闭原则。

**解决方案**：
- 采用**工厂+策略模式**，定义 `CouponScopeStrategy` 接口
- 实现 3 种策略：`AllProductCouponScopeStrategy`（全场通用）、`CategoryCouponScopeStrategy`（指定分类）、`ProductCouponScopeStrategy`（指定商品）
- 通过 `CouponScopeStrategyFactory` 动态路由，**YAML 配置文件映射策略类型**

**结果**：新增优惠券适用范围只需添加策略类 + 配置一行，**零代码改动**，符合开闭原则。

### 难点 4：AI Agent 响应延迟与幻觉问题

**问题**：原生 LLM 响应慢（平均 5s+），且容易"胡说八道"，用户体验差，Token 成本高。

**解决方案**：
- 设计 **L1/L2/L3 三级路由架构**：L1 正则匹配（<5ms，拦截 90% 高频指令），L2 对话状态机（多轮交互场景），L3 LLM 兜底（~2s）
- 代码层**空数据兜底**，避免无效查询消耗 Token
- 构建 **RAG 知识库**（ChromaDB + DashScope Embedding），基于知识库回答专业问题

**结果**：90% 高频指令响应延迟 <5ms，**零 LLM 成本**；综合响应速度提升 60%，Token 成本降低 40%。

---

## 模块架构

```
nova-mall/
├── nova-mall-common/              # 公共模块：工具类、异常处理、统一返回结果、Redis封装
├── nova-mall-security/            # 安全模块：JWT鉴权、RBAC动态权限、Spring Security配置
├── nova-mall-admin/               # 后台管理服务 (8080)
│   ├── controller/                # 34个REST Controller（商品/订单/营销/用户/Agent查询）
│   ├── service/                   # 33个Service接口 + 实现类
│   └── resources/
│       ├── application-dev.yml    # 开发环境配置（MySQL/Redis/MinIO）
│       └── dao/                   # MyBatis XML映射文件
├── nova-mall-portal/              # 商城前台服务 (8085)
│   ├── controller/                # 14个REST Controller（购物车/订单/支付/会员）
│   ├── domain/
│   │   ├── order/                 # 订单责任链模式（24个Handler）
│   │   │   ├── OrderHandler.java      # 抽象处理者
│   │   │   ├── OrderCreationChain.java # 链执行器
│   │   │   └── handler/               # 具体Handler实现
│   │   ├── promotion/             # 促销策略模式（5种策略）
│   │   ├── coupon/                # 优惠券适用范围策略（3种策略）
│   │   └── payment/               # 支付网关策略（支付宝）
│   └── resources/
│       └── application-dev.yml    # MySQL/Redis/MongoDB/RabbitMQ/支付宝
├── nova-mall-search/              # 搜索服务 (8081)
│   ├── controller/
│   │   └── EsProductController.java   # 商品搜索API
│   └── service/
│       └── impl/
│           └── EsClientService.java   # ES客户端封装
└── deploy/                        # Docker部署配置
    ├── Dockerfile
    └── docker-compose-app.yml
```

### 依赖关系

```
common ← security ← admin
                  ← portal
common ← search
```

### 核心模块说明

| 模块 | 职责 | 技术亮点 |
|------|------|---------|
| common | 公共组件、异常处理、Redis封装 | 统一返回结构、AOP日志切面 |
| security | JWT鉴权、动态权限 | URL级别权限控制、Token自动续期 |
| admin | 后台管理API（34个Controller） | RBAC动态权限、Druid监控 |
| portal | 商城前台API（14个Controller） | 责任链模式、策略模式、秒杀 |
| search | ES全文搜索 | IK中文分词、多维度检索 |

---

## 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.5.14 |
| 安全框架 | Spring Security + JWT | — |
| ORM | MyBatis-Plus | 3.5.15 |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis | 6.x+ |
| 文档数据库 | MongoDB（Portal 模块） | 5.x+ |
| 搜索引擎 | Elasticsearch | 7.17.3 |
| 消息队列 | RabbitMQ（Portal 模块） | 3.x |
| 对象存储 | MinIO / 阿里云 OSS | — |
| 支付 | 支付宝 SDK | 4.40.630 |
| 连接池 | Druid | 1.2.24 |
| API 文档 | SpringDoc OpenAPI | 2.8.17 |
| 工具库 | Hutool | 5.8.40 |
| Java 版本 | JDK | 17 |

---

## 各模块说明

### nova-mall-common — 公共模块

提供所有模块共享的基础组件，被 `security` 和 `search` 直接依赖。

```
com.su.mall.common/
├── api/                          # 统一返回结构
│   ├── CommonResult.java         # 通用返回封装（code + message + data）
│   ├── CommonPage.java           # 分页返回封装
│   ├── PageResult.java           # 分页数据封装
│   ├── ResultCode.java           # 状态码枚举（SUCCESS/FAILED/UNAUTHORIZED 等）
│   ├── IResultCode.java          # 状态码接口
│   └── IErrorCode.java           # 错误码接口
├── exception/                    # 异常处理
│   ├── GlobalExceptionHandler.java  # 全局异常拦截（@RestControllerAdvice）
│   ├── ApiException.java         # 业务异常
│   └── Asserts.java              # 断言工具（Asserts.fail 快速抛业务异常）
├── service/
│   ├── RedisService.java         # Redis 操作接口
│   └── impl/RedisServiceImpl.java
├── config/
│   └── BaseRedisConfig.java      # Redis 序列化配置
├── annotation/
│   └── ApiLog.java               # 接口日志注解
├── log/
│   └── WebLogAspect.java         # AOP 接口日志切面（@ApiLog 触发）
├── domain/
│   ├── WebLog.java               # 日志数据对象
│   └── SwaggerProperties.java    # Swagger 属性配置
└── util/
    └── RequestUtil.java          # 请求工具类
```

**核心依赖**：MyBatis-Plus（分页插件）、Redis、SpringDoc OpenAPI、Validation

### nova-mall-security — 安全模块

基于 Spring Security + JWT 的认证授权框架，支持动态 URL 级别权限控制。被 `admin` 和 `portal` 模块依赖。

```
com.su.mall.security/
├── config/
│   ├── SecurityConfig.java           # SecurityFilterChain 配置（核心）
│   ├── CommonSecurityConfig.java     # 通用安全配置
│   ├── IgnoreUrlsConfig.java         # 白名单路径配置（@ConfigurationProperties）
│   └── RedisConfig.java              # Redis 缓存配置
├── component/
│   ├── JwtAuthenticationTokenFilter.java   # JWT 过滤器（请求头提取 Token → 校验 → 放入 SecurityContext）
│   ├── DynamicAuthorizationManager.java    # 动态权限管理器（URL 级别授权）
│   ├── DynamicSecurityMetadataSource.java  # 动态权限元数据（从数据库加载资源列表）
│   ├── DynamicSecurityService.java         # 动态权限服务接口
│   ├── CustomConfigAttribute.java          # 自定义权限属性
│   ├── RestfulAccessDeniedHandler.java     # 未授权处理器（返回 JSON）
│   └── RestAuthenticationEntryPoint.java   # 未认证处理器（返回 JSON）
├── util/
│   ├── JwtTokenUtil.java             # JWT 工具类（生成/解析/校验 Token）
│   └── SpringUtil.java               # Spring 上下文工具
├── aspect/
│   └── RedisCacheAspect.java         # Redis 缓存切面（@CacheException 注解触发）
└── annotation/
    └── CacheException.java           # 缓存异常注解
```

**认证流程**：请求 → JwtAuthenticationTokenFilter（校验 Token）→ DynamicAuthorizationManager（校验 URL 权限）→ Controller

### nova-mall-admin — 后台管理服务

后台管理 API 服务，端口 `8080`。提供商品、订单、营销、用户、内容等全业务管理接口。

```
com.su.mall/
├── MallAdminApplication.java    # 启动入口
├── controller/                   # 34 个 REST Controller
│   ├── Pms*Controller           # 商品管理（商品/品牌/分类/属性/SKU库存/操作日志/审核）
│   ├── Oms*Controller           # 订单管理（订单/退货申请/退货原因/订单设置/公司地址）
│   ├── Sms*Controller           # 营销管理（优惠券/秒杀活动/场次/广告/品牌推荐/新品/热销/专题）
│   ├── Ums*Controller           # 用户管理（管理员/角色/菜单/资源/资源分类/会员等级）
│   ├── Cms*Controller           # 内容管理（专题/优选专区）
│   ├── OssController            # 阿里云 OSS 上传
│   ├── MinioController          # MinIO 文件上传
│   └── AgentQueryController     # Agent 查询接口
├── service/                      # 33 个 Service 接口 + 34 个实现类
├── model/                        # 数据模型（POJO）
├── mapper/                       # MyBatis Mapper 接口
├── validator/                    # 自定义校验注解（@FlagValidator）
└── resources/
    ├── application.yml           # 主配置
    ├── application-dev.yml       # 开发环境（MySQL/Redis/MinIO）
    ├── application-prod.yml      # 生产环境
    └── dao/                      # MyBatis XML 映射文件
```

**技术亮点**：
- RBAC 动态权限：从数据库加载资源列表，运行时变更权限无需重启
- Druid 连接池监控：`/druid` 查看 SQL 执行情况
- Swagger API 文档：`/swagger-ui.html`

### nova-mall-portal — 商城前台服务

商城前台 API 服务，端口 `8085`。提供面向用户的购物、订单、支付、会员等接口。

```
com.su.mall.portal/
├── MallPortalApplication.java    # 启动入口
├── controller/                   # 14 个 REST Controller
│   ├── HomeController            # 首页数据（轮播图/品牌/新品/秒杀）
│   ├── PmsPortal*Controller      # 商品（详情/列表/品牌）
│   ├── OmsCartItemController     # 购物车 CRUD
│   ├── OmsPortalOrderController  # 订单（确认/生成/列表/详情/取消/收货）
│   ├── OmsPortalOrderReturn*     # 售后（申请/列表）
│   ├── OmsOrderCommentController # 商品评价
│   ├── UmsMember*Controller      # 会员（登录/注册/信息/地址/优惠券）
│   ├── Member*Controller         # 收藏/关注/浏览历史
│   └── AlipayController          # 支付宝支付
├── service/                      # 16 个 Service 接口 + 实现类
├── domain/
│   ├── order/                    # 订单责任链模式
│   │   ├── OrderHandler.java         # 抽象处理者
│   │   ├── OrderCreationChain.java   # 链执行器
│   │   ├── OrderHandlerContext.java  # 上下文（传递参数）
│   │   └── handler/                  # 23 个具体 Handler
│   │       ├── ValidateAddressHandler    # 校验地址
│   │       ├── LoadCartHandler           # 加载购物车
│   │       ├── FlashValidationHandler    # 秒杀校验
│   │       ├── BuildOrderItemsHandler    # 构建订单项
│   │       ├── CheckStockHandler         # 校验库存
│   │       ├── HandleCouponHandler       # 处理优惠券
│   │       ├── HandleIntegrationHandler  # 处理积分
│   │       ├── HandleRealAmountHandler   # 计算实付金额
│   │       ├── LockStockHandler          # 锁定库存
│   │       ├── BuildOrderHandler         # 构建订单
│   │       ├── PersistOrderHandler       # 持久化订单
│   │       ├── PostOrderHandler          # 订单后续处理（MQ 延迟队列）
│   │       ├── UpdatePaymentStatusHandler # 更新支付状态
│   │       ├── LoadOrderDetailHandler    # 加载订单明细（支付流程）
│   │       ├── DeductStockHandler        # 扣减库存
│   │       ├── UpdateSalesHandler        # 更新销量
│   │       ├── UpdateCouponStatusForPayHandler # 支付后更新优惠券
│   │       ├── LoadOrderHandler          # 加载订单（取消流程）
│   │       ├── LoadAndValidateCancelOrderHandler # 加载并校验取消订单
│   │       ├── UpdateCancelStatusHandler # 更新取消状态
│   │       ├── RestoreStockForCancelHandler # 恢复库存
│   │       ├── RestoreCouponForCancelHandler # 恢复优惠券
│   │       └── ReturnIntegrationHandler  # 返还积分
│   ├── promotion/                # 促销策略模式
│   │   ├── PromotionStrategy.java       # 策略接口
│   │   ├── PromotionContext.java        # 策略上下文
│   │   ├── PromotionStrategyFactory.java # 策略工厂
│   │   ├── NoPromotionStrategy.java     # 无优惠
│   │   ├── SingleItemPromotionStrategy.java  # 单品促销
│   │   ├── LadderDiscountPromotionStrategy.java # 阶梯折扣
│   │   ├── FullReductionPromotionStrategy.java  # 满减
│   │   └── FlashPromotionStrategy.java   # 秒杀
│   ├── coupon/                   # 优惠券适用范围策略
│   │   ├── CouponScopeStrategy.java     # 策略接口
│   │   ├── CouponScopeStrategyFactory.java # 策略工厂
│   │   ├── AllProductCouponScopeStrategy.java  # 全场通用
│   │   ├── CategoryCouponScopeStrategy.java    # 指定分类
│   │   └── ProductCouponScopeStrategy.java     # 指定商品
│   └── payment/                  # 支付策略模式
│       ├── PaymentStrategy.java         # 策略接口
│       ├── PaymentStrategyFactory.java  # 策略工厂
│       ├── PaymentParam.java            # 支付参数
│       └── AlipayPaymentStrategy.java   # 支付宝支付
├── repository/                   # MongoDB Repository
│   ├── MemberBrandAttentionRepository.java
│   ├── MemberProductCollectionRepository.java
│   └── MemberReadHistoryRepository.java
└── resources/
    ├── application.yml
    ├── application-dev.yml       # MySQL/Redis/MongoDB/RabbitMQ/支付宝
    └── dao/                      # MyBatis XML 映射文件
```

**设计模式**：
- **责任链模式**：订单生成（13 个 Handler）、支付成功（6 个 Handler）、订单取消（5 个 Handler）
- **工厂+策略模式**：促销计算（5 种策略）、优惠券适用范围（3 种策略）、支付网关（支付宝）

### nova-mall-search — 搜索服务

Elasticsearch 全文搜索服务，端口 `8081`。商品数据从 MySQL 同步至 ES，提供多维度搜索。

```
com.su.mall.search/
├── MallSearchApplication.java    # 启动入口
├── controller/
│   └── EsProductController.java  # 商品搜索 API
├── service/
│   ├── EsProductService.java     # 搜索服务接口
│   └── impl/
│       ├── EsProductServiceImpl.java  # 搜索实现
│       └── EsClientService.java       # ES 客户端封装
├── domain/
│   ├── EsProduct.java                # ES 商品文档
│   ├── EsProductAttributeValue.java  # 商品属性值
│   └── EsProductRelatedInfo.java     # 关联信息（品牌/分类聚合）
├── dao/
│   └── EsProductDao.java         # 从 MySQL 读取商品数据
├── mapper/                       # MyBatis Mapper（读取 MySQL 数据同步到 ES）
└── config/
    ├── ElasticsearchClientConfig.java  # ES 客户端配置
    ├── MyBatisConfig.java              # MyBatis 配置
    └── SpringDocConfig.java            # Swagger 配置
```

**搜索能力**：按商品名称、品牌、分类进行全文检索，支持 IK 中文分词。

---

## 快速启动

### 环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 必须 |
| Maven | 3.8+ | 必须 |
| Node.js | 20+ | 前端开发需要 |
| MySQL | 8.x | 必须 |
| Redis | 6.x+ | 必须（秒杀库存预减） |
| MongoDB | 5.x+ | Portal 模块需要 |
| Elasticsearch | 7.17.x | Search 模块需要 |
| RabbitMQ | 3.x | Portal 模块需要（延迟队列） |

### 1. 数据库初始化

```bash
# 导入初始化脚本
mysql -u root -p < document/init.sql
```

### 2. 配置文件

修改各模块 `src/main/resources/application-dev.yml`，填入实际连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为实际密码
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password  # 修改为实际密码
```

**重要**：不要将真实的 API Key 和密码提交到 GitHub！

### 3. 打包公共模块

首次启动必须先打包 common 和 security：

```bash
cd nova-mall
mvn clean install -pl nova-mall-common,nova-mall-security -am
```

### 4. 启动服务

```bash
# 后台管理 (8080)
mvn spring-boot:run -pl nova-mall-admin

# 商城前台 (8085)
mvn spring-boot:run -pl nova-mall-portal

# 搜索服务 (8081)
mvn spring-boot:run -pl nova-mall-search
```

### 5. 访问 API 文档

| 服务 | Swagger 地址 |
|------|-------------|
| Admin (8080) | http://localhost:8080/swagger-ui.html |
| Portal (8085) | http://localhost:8085/swagger-ui.html |
| Search (8081) | http://localhost:8081/swagger-ui.html |

### 默认账号

```
用户名：admin
密  码：123456
```

### Docker 部署（推荐）

```bash
cd deploy
docker compose -f docker-compose-app.yml up -d --build
```

---

## 数据库表结构

项目按业务域划分数据库表前缀：

| 前缀 | 业务域 | 说明 |
|------|--------|------|
| `pms_` | PMS | 商品管理（商品、品牌、分类、属性、SKU 库存） |
| `oms_` | OMS | 订单管理（订单、订单项、购物车、退货） |
| `sms_` | SMS | 营销管理（优惠券、秒杀、广告、推荐） |
| `ums_` | UMS | 用户管理（管理员、角色、资源、会员） |
| `cms_` | CMS | 内容管理（专题、优选专区） |

完整建表脚本见 `document/init.sql`。

---

## 设计模式应用

详见 [DESIGN_PATTERN_REFACTOR.md](./DESIGN_PATTERN_REFACTOR.md)。

| 模式 | 应用场景 | 实现 |
|------|---------|------|
| 工厂+策略 | 促销计算 | `PromotionStrategyFactory` → 5 种策略，配置文件映射 |
| 工厂+策略 | 优惠券范围 | `CouponScopeStrategyFactory` → 3 种策略 |
| 工厂+策略 | 支付网关 | `PaymentStrategyFactory` → 支付宝（可扩展微信） |
| 责任链 | 订单生成 | `OrderCreationChain` → 13 个 Handler |
| 责任链 | 支付成功 | 同上 → 6 个 Handler |
| 责任链 | 订单取消 | 同上 → 5 个 Handler |

---

## 项目截图(部分功能)

### 后台管理（Admin）

#### 后台首页

数据概览 + ECharts 统计图表

<p align="center">
  <img src="document/resource/screenshot-admin-home.png" width="80%" alt="后台首页">
</p>

#### 优惠券管理

创建/编辑/领取历史

<p align="center">
  <img src="document/resource/screenshot-admin-coupon_1.png" width="30%" alt="优惠券列表">
  <img src="document/resource/screenshot-admin-coupon_2.png" width="30%" alt="创建优惠券">
  <img src="document/resource/screenshot-admin-coupon_3.png" width="30%" alt="领取历史">
</p>

#### 秒杀管理

活动创建/场次/商品关联

<p align="center">
  <img src="document/resource/screenshot-admin-flash_1.png" width="30%" alt="秒杀活动">
  <img src="document/resource/screenshot-admin-flash_2.png" width="30%" alt="秒杀场次">
  <img src="document/resource/screenshot-admin-flash_3.png" width="30%" alt="秒杀商品关联">
</p>

#### 订单管理

状态筛选 + 详情弹窗

<p align="center">
  <img src="document/resource/screenshot-admin-order_1.png" width="30%" alt="订单列表">
  <img src="document/resource/screenshot-admin-order_2.png" width="30%" alt="订单详情">
</p>

#### 商品管理

列表筛选 + 多步骤表单

<p align="center">
  <img src="document/resource/screenshot-admin-product_1.png" width="18%" alt="商品列表">
  <img src="document/resource/screenshot-admin-product_2.png" width="18%" alt="商品信息">
  <img src="document/resource/screenshot-admin-product_3.png" width="18%" alt="商品属性">
  <img src="document/resource/screenshot-admin-product_4.png" width="18%" alt="商品详情">
  <img src="document/resource/screenshot-admin-product_5.png" width="18%" alt="商品预览">
</p>

#### 权限管理

角色-资源分配

<p align="center">
  <img src="document/resource/screenshot-admin-role_1.png" width="30%" alt="角色列表">
  <img src="document/resource/screenshot-admin-role_2.png" width="30%" alt="资源分配">
</p>

### 移动端商城（App）

#### 首页

轮播图 + 分类导航 + 推荐商品

<p align="center">
  <img src="document/resource/screenshot-app-home_1.png" width="30%" alt="首页-轮播图">
  <img src="document/resource/screenshot-app-home_2.png" width="30%" alt="首页-分类导航">
  <img src="document/resource/screenshot-app-home_3.png" width="30%" alt="首页-推荐商品">
</p>

#### 秒杀活动

限时秒杀 + 倒计时 + 抢购

<p align="center">
  <img src="document/resource/screenshot-app-flash_1.png" width="30%" alt="秒杀列表">
  <img src="document/resource/screenshot-app-flash_2.png" width="30%" alt="秒杀详情">
</p>

#### 优惠券

领取优惠券 + 我的优惠券

<p align="center">
  <img src="document/resource/screenshot-app-coupon_1.png" width="30%" alt="优惠券列表">
  <img src="document/resource/screenshot-app-coupon_2.png" width="30%" alt="我的优惠券">
</p>

#### 购物车

商品列表 + 数量修改 + 结算

<p align="center">
  <img src="document/resource/screenshot-app-cart.png" width="24%" alt="购物车">
</p>

#### 商品详情

规格选择 + 加购

<p align="center">
  <img src="document/resource/screenshot-app-product_1.png" width="30%" alt="商品详情">
  <img src="document/resource/screenshot-app-product_2.png" width="30%" alt="商品规格">
</p>

#### 订单列表

<p align="center">
  <img src="document/resource/screenshot-app-order_1.png" width="30%" alt="订单列表">
  <img src="document/resource/screenshot-app-order_2.png" width="30%" alt="订单详情">
</p>

#### 售后服务

退货申请 + 售后进度

<p align="center">
  <img src="document/resource/screenshot-app-return.png" width="30%" alt="售后服务">
</p>

#### 会员中心

个人信息 + 积分 + 收藏

<p align="center">
  <img src="document/resource/screenshot-app-user.png" width="30%" alt="会员中心">
</p>

### AI 智能客服（Agent）

自然语言对话交互 + 自动聚合统计工具

<p align="center">
  <img src="document/resource/screenshot-agent-chat.gif" width="45%" alt="智能客服对话">
  <img src="document/resource/screenshot-agent-daily.gif" width="45%" alt="运营日报">
</p>

---

Dockerfile 和 docker-compose 配置见 `deploy/` 目录，完整部署方案见 [DEPLOYMENT.md](../DEPLOYMENT.md)。
