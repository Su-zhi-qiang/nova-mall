# Nova-Mall 项目单元测试说明

## 📋 测试概览

本项目已完成 MyBatis-Plus 改造，并完成**MyBatis-Plus 分页插件改造**，所有单元测试已重新编写以验证改造后的功能。

### 🎯 改造里程碑

#### 第一阶段：MyBatis-Plus 基础改造（已完成）
- 移除 PageHelper 依赖
- 配置 MyBatis-Plus 分页插件 PaginationInnerInterceptor
- 改造所有 Service 层分页方法使用 MP 的 Page 对象
- 改造所有 Controller 层分页返回使用 CommonPage.restPage()

#### 第二阶段：MyBatis-Plus 分页插件改造（已完成）
- 统一替换所有 `PageHelper.startPage()` 为 MP 分页
- 配置数据库类型 MySQL、单页最大限制 500、分页溢出处理
- 新增 `mybatis-plus-jsqlparser-4.9` 依赖支持
- 更新 CommonPage 支持 MP Page 和 Spring Data Page
- 改造 27+ 个 Service/Controller 类
- 更新所有测试类适配新的分页接口

#### nova-mall-admin 模块
- PmsBrandService / PmsBrandServiceImpl / PmsBrandController
- PmsProductService / PmsProductServiceImpl / PmsProductController
- PmsProductCategoryService / PmsProductCategoryServiceImpl / PmsProductCategoryController
- SmsHomeBrandService / SmsHomeBrandServiceImpl / SmsHomeBrandController
- SmsHomeRecommendSubjectService / SmsHomeRecommendSubjectServiceImpl / SmsHomeRecommendSubjectController
- SmsFlashPromotionProductRelationService / SmsFlashPromotionProductRelationServiceImpl / SmsFlashPromotionProductRelationController
- OmsOrderService / OmsOrderServiceImpl / OmsOrderController
- 以及其他 17+ 个 Service/Controller 类

#### nova-mall-portal 模块
- PmsPortalBrandService / PmsPortalBrandServiceImpl / PmsPortalBrandController
- HomeService / HomeServiceImpl / HomeController
- OmsPortalOrderService / OmsPortalOrderServiceImpl / OmsPortalOrderController
- MemberCollectionService / MemberCollectionServiceImpl
- MemberAttentionService / MemberAttentionServiceImpl
- MemberReadHistoryService / MemberReadHistoryServiceImpl

#### nova-mall-demo 模块
- DemoService / DemoServiceImpl / DemoController

### 🎯 测试文件清单

#### nova-mall-admin 模块

| 测试文件 | 说明 | 覆盖范围 |
|---------|------|---------|
| `MyBatisPlusTests.java` | Service 层完整测试 | PmsBrandService CRUD + MP 分页 |
| `MyBatisPlusMapperTests.java` | Mapper 层基础测试 | PmsBrandMapper CRUD + LambdaQueryWrapper |
| `MultiModuleMapperTests.java` | 多模块 Mapper 测试 | 6 个核心 Mapper 的 CRUD |
| `MultiModuleServiceTests.java` | 多模块 Service 测试 | 3 个核心 Service + MP 分页 |
| `PmsDaoTests.java` | 手写 DAO 测试 | 复杂查询验证 |

#### nova-mall-portal 模块

| 测试文件 | 说明 | 覆盖范围 |
|---------|------|---------|
| `MyBatisPlusPortalTests.java` | Portal 模块测试 | 品牌查询 + MP 分页 |
| `PortalProductDaoTests.java` | 手写 DAO 测试 | 复杂查询验证 |
| `MallPortalApplicationTests.java` | 应用启动测试 | 基础配置验证 |

#### nova-mall-search 模块

| 测试文件 | 说明 | 覆盖范围 |
|---------|------|---------|
| `MallSearchMapperTests.java` | Search 模块测试 | Mapper + ES 数据准备 |
| `MallSearchApplicationTests.java` | 应用启动测试 | 基础配置验证 |

#### nova-mall-demo 模块

| 测试文件 | 说明 | 覆盖范围 |
|---------|------|---------|
| `MallDemoApplicationTests.java` | 应用启动测试 | 基础配置验证 |

---

## 🔧 MyBatis-Plus 分页配置

### 配置文件位置

**配置类**: `nova-mall-admin/src/main/java/com/su/mall/config/MyBatisConfig.java`

```java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
    paginationInnerInterceptor.setMaxLimit(500L); // 单页最多 500 条，防刷
    paginationInnerInterceptor.setOverflow(true);  // 页码超出总页返回第一页
    interceptor.addInnerInterceptor(paginationInnerInterceptor);
    return interceptor;
}
```

### CommonPage 使用方式

**更新文件**: `nova-mall-common/src/main/java/com/su/mall/common/api/CommonPage.java`

```java
// 方式 1: PageHelper 分页结果
CommonPage<T> restPage(List<T> list)

// 方式 2: MyBatis-Plus Page 分页结果（推荐）
CommonPage<T> restPage(Page<T> page)

// 方式 3: Spring Data Page 分页结果
CommonPage<T> restPage(org.springframework.data.domain.Page<T> page)
```

### Service 层改造示例

**改造前（PageHelper）**:
```java
public List<PmsBrand> listBrand(String keyword, Integer showStatus, int pageNum, int pageSize) {
    PageHelper.startPage(pageNum, pageSize);
    return pmsBrandMapper.selectByExample(example);
}
```

**改造后（MP 分页）**:
```java
public Page<PmsBrand> listBrand(String keyword, Integer showStatus, int pageNum, int pageSize) {
    LambdaQueryWrapper<PmsBrand> wrapper = new LambdaQueryWrapper<>();
    if (StrUtil.isNotEmpty(keyword)) {
        wrapper.like(PmsBrand::getName, keyword);
    }
    if (showStatus != null) {
        wrapper.eq(PmsBrand::getShowStatus, showStatus);
    }
    return pmsBrandMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
}
```

### Controller 层改造示例

**改造前**:
```java
public CommonResult<CommonPage<PmsBrand>> list(...) {
    List<PmsBrand> list = brandService.listBrand(...);
    return CommonResult.success(CommonPage.restPage(list));
}
```

**改造后**:
```java
public CommonResult<CommonPage<PmsBrand>> list(...) {
    Page<PmsBrand> page = brandService.listBrand(...);
    return CommonResult.success(CommonPage.restPage(page));
}
```

---

## 🚀 运行测试

### 前置条件

1. **数据库必须启动**（MySQL）
2. **数据库已初始化**（运行 `document/sql/mall.sql`）
3. **Maven 已安装**（推荐 3.6.3+）
4. **JDK 17+**
5. **MP 分页插件依赖已安装**（mybatis-plus-jsqlparser-4.9）

### 运行方式

#### 方式 1：在 IDE 中运行（推荐）

**IntelliJ IDEA：**

1. 打开测试文件
2. 右键点击类名或方法名
3. 选择 "Run '类名'" 或 "Debug '类名'"
4. 查看控制台输出

**常用测试类：**

- `MyBatisPlusTests` - Admin Service 层完整测试（含 MP 分页）
- `MyBatisPlusMapperTests` - Admin Mapper 层测试
- `MultiModuleMapperTests` - 多模块 Mapper 测试
- `MultiModuleServiceTests` - 多模块 Service 测试（含 MP 分页）
- `MyBatisPlusPortalTests` - Portal 模块测试（含 MP 分页）

#### 方式 2：使用 Maven 命令

```bash
# 进入项目根目录
cd d:\A\GithubCode\mall\nova-mall

# 先编译项目（确保改造后的代码正确）
mvn compile -DskipTests

# 运行所有模块的测试
mvn test

# 运行特定模块的测试
cd nova-mall-admin
mvn test

# 运行特定测试类
mvn test -Dtest=MyBatisPlusTests

# 运行特定测试方法
mvn test -Dtest=MyBatisPlusTests#testPageQueryBrands
```

#### 方式 3：在 VS Code 中运行

1. 安装 "Java Test Runner" 扩展
2. 打开测试文件
3. 点击测试类或方法上方的 "Run Test" / "Debug Test" 按钮

---

## 📊 测试详情

### MyBatisPlusTests (Service 层)

```java
测试覆盖：
✅ testListAllBrands()          - 查询所有品牌
✅ testPageQueryBrands()        - MP 分页查询 + 条件查询 + CommonPage 转换
✅ testCreateBrand()            - 新增品牌
✅ testGetBrandById()           - 根据 ID 查询
✅ testUpdateBrand()            - 更新品牌
✅ testDeleteBrand()            - 批量删除
✅ testUpdateShowStatus()       - 批量更新显示状态
✅ testUpdateFactoryStatus()    - 批量更新厂家状态
```

### MyBatisPlusMapperTests (Mapper 层)

```java
测试覆盖：
✅ testSelectList()             - selectList 查询
✅ testSelectById()             - selectById 查询
✅ testInsert()                 - insert 新增
✅ testUpdateById()             - updateById 更新
✅ testDeleteById()             - deleteById 删除
✅ testLambdaQueryWrapper()     - LambdaQueryWrapper 条件查询
```

### MultiModuleMapperTests (多模块 Mapper)

```java
测试覆盖：
✅ testCategoryMapper()         - PmsProductCategoryMapper
✅ testSkuStockMapper()         - PmsSkuStockMapper
✅ testHomeBrandMapper()        - SmsHomeBrandMapper
✅ testCouponMapper()          - SmsCouponMapper (含新增)
✅ testMenuMapper()            - UmsMenuMapper
✅ testAdminMapper()           - UmsAdminMapper
✅ testLambdaUpdateWrapper()    - LambdaUpdateWrapper 批量更新
✅ testDeleteById()            - deleteById 删除操作
```

### MultiModuleServiceTests (多模块 Service)

```java
测试覆盖：
✅ testCategoryService()        - PmsProductCategoryService（MP 分页）
✅ testBrandService()           - PmsBrandService（MP 分页）
✅ testHomeBrandService()       - SmsHomeBrandService（MP 分页）
✅ testCreateBrand()            - Mapper insert 新增
✅ testUpdateBrand()            - Mapper updateById 更新
✅ testComplexQuery()          - LambdaQueryWrapper 复杂查询
✅ testBatchDelete()           - LambdaQueryWrapper 批量删除
✅ testPageQuery()            - MP 分页查询验证 + CommonPage 转换
```

### MyBatisPlusPortalTests (Portal 模块)

```java
测试覆盖：
✅ testRecommendListBrands()    - Portal 品牌查询（MP 分页 + CommonPage 转换）
✅ testBrandDetail()           - 品牌详情查询
✅ testMapperBasicQuery()      - Mapper 基础查询验证
✅ testProductList()           - 品牌关联商品分页查询
```

---

## ⚠️ 常见问题

### 1. Maven 版本问题

**错误信息：**
```
Maven Surefire Plugin requires Maven version 3.6.3
```

**解决方案：**
- 升级 Maven 到 3.6.3+
- 或直接在 IDE 中运行测试

### 2. 数据库连接问题

**错误信息：**
```
Connection refused or Database does not exist
```

**解决方案：**
- 确保 MySQL 已启动
- 检查 `application-dev.yml` 中的数据库配置
- 确保数据库已创建：`CREATE DATABASE mall`

### 3. 端口占用问题

**错误信息：**
```
Port 8080 was already in use
```

**解决方案：**
- 修改 `application.yml` 中的端口号
- 或停止占用端口的进程

### 4. MP 分页插件依赖问题

**错误信息：**
```
java.lang.NoClassDefFoundError: net/jsqlparser/statement/select/Select
```

**解决方案：**
- 确保 `nova-mall-common/pom.xml` 中已添加 `mybatis-plus-jsqlparser-4.9` 依赖
- 运行 `mvn clean install` 重新安装依赖
- 检查 `MyBatisConfig.java` 中分页插件配置是否正确

### 5. Page 类型导入问题

**错误信息：**
```
java: 不兼容的类型: com.baomidou.mybatisplus.extension.plugins.pagination.Page<...>
      无法转换为 java.util.List<...>
```

**解决方案：**
- 检查 Service 接口和实现类是否都返回 `Page<T>` 而不是 `List<T>`
- 检查测试类是否正确使用 `page.getRecords()` 获取列表数据
- 确保 `import com.baomidou.mybatisplus.extension.plugins.pagination.Page;` 已添加

---

## 📈 测试预期结果

### ✅ 成功标志

1. **所有测试通过**（绿色勾选）
2. **无异常抛出**
3. **控制台输出**（MP 分页）：
   ```
   ====== 测试2：分页查询品牌 ==========
   查询到 10 个品牌，总记录数: 5
   CommonPage 转换成功，总页数: 1, 当前页: 1
   ```

### ❌ 失败排查

1. **检查数据库连接**
2. **检查数据是否存在**
3. **检查 MP 分页插件配置**
4. **检查 Service 方法返回类型**
5. **查看详细错误日志**
6. **使用 Debug 模式调试**

---

## 🎓 测试最佳实践

### 1. 隔离测试数据

```java
@Test
@Transactional
@Rollback
public void testCreate() {
    // 测试代码
    // 测试结束后自动回滚，不影响数据库
}
```

### 2. 有意义的测试名称

```java
@Test
@DisplayName("测试新增品牌 - MyBatis-Plus insert")
public void testCreateBrand() {
    // 测试代码
}
```

### 3. 详细的日志输出

```java
LOGGER.info("查询到品牌数量: {}", page.getRecords().size());
LOGGER.info("总记录数: {}", page.getTotal());
LOGGER.info("新增品牌成功，ID: {}", brand.getId());
```

### 4. 明确的断言

```java
assertNotNull(page, "分页对象不应为 null");
assertNotNull(page.getRecords(), "品牌列表不应为 null");
assertTrue(page.getTotal() > 0, "应该至少有一个品牌");
```

### 5. MP 分页最佳实践

```java
// 1. 使用 selectPage 而不是 selectList + PageHelper
Page<PmsBrand> page = new Page<>(pageNum, pageSize);
Page<PmsBrand> result = brandMapper.selectPage(page, wrapper);

// 2. 使用 CommonPage.restPage() 转换为通用分页对象
CommonPage<PmsBrand> commonPage = CommonPage.restPage(page);

// 3. 获取分页数据使用 getRecords()
List<PmsBrand> list = page.getRecords();

// 4. 获取分页元数据
long total = page.getTotal();
long current = page.getCurrent();
long pages = page.getPages();
```

---

## 📝 修改测试数据

如果需要修改测试数据，请编辑对应的 SQL 文件：

- **测试数据 SQL**：`document/sql/mall.sql`
- **测试品牌**：`pms_brand` 表
- **测试分类**：`pms_product_category` 表
- **测试优惠券**：`sms_coupon` 表
- **测试商品**：`pms_product` 表

---

## 🔄 持续集成

建议配置 CI/CD 流程自动运行测试：

```yaml
# GitHub Actions 示例
name: Unit Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Cache Maven packages
        uses: actions/cache@v2
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
      - name: Build with Maven
        run: mvn compile -DskipTests
      - name: Run tests
        run: mvn test
```

---

## 📞 获取帮助

如果测试运行过程中遇到问题：

1. 查看详细错误日志
2. 检查数据库连接配置
3. 确认 MP 分页插件配置正确
4. 确认测试数据是否存在
5. 查看 [MyBatis-Plus 官方文档](https://baomidou.com/)

---

## 📚 相关文档

- **MP 配置说明**: `nova-mall-admin/src/main/java/com/su/mall/config/MyBatisConfig.java`
- **CommonPage 工具类**: `nova-mall-common/src/main/java/com/su/mall/common/api/CommonPage.java`
- **项目 POM 依赖**: `nova-mall-common/pom.xml`
- **测试类**: `nova-mall-admin/src/test/java/com/su/mall/` 和 `nova-mall-portal/src/test/java/com/su/mall/portal/`

---

**MP 分页插件改造已完成！祝测试愉快！🎉**
