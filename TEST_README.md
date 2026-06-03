# Nova-Mall 项目单元测试说明

## 📋 测试概览

本项目已完成 MyBatis-Plus 改造，所有单元测试已重新编写以验证改造后的功能。

### 🎯 测试文件清单

#### nova-mall-admin 模块

| 测试文件 | 说明 | 覆盖范围 |
|---------|------|---------|
| `MyBatisPlusTests.java` | Service 层完整测试 | PmsBrandService CRUD + 分页 |
| `MyBatisPlusMapperTests.java` | Mapper 层基础测试 | PmsBrandMapper CRUD + LambdaQueryWrapper |
| `MultiModuleMapperTests.java` | 多模块 Mapper 测试 | 6 个核心 Mapper 的 CRUD |
| `MultiModuleServiceTests.java` | 多模块 Service 测试 | 3 个核心 Service + LambdaQueryWrapper |
| `PmsDaoTests.java` | 手写 DAO 测试 | 复杂查询验证 |

#### nova-mall-portal 模块

| 测试文件 | 说明 | 覆盖范围 |
|---------|------|---------|
| `MyBatisPlusPortalTests.java` | Portal 模块测试 | 品牌查询 + Mapper 基础 |
| `PortalProductDaoTests.java` | 手写 DAO 测试 | 复杂查询验证 |
| `MallPortalApplicationTests.java` | 应用启动测试 | 基础配置验证 |

#### nova-mall-search 模块

| 测试文件 | 说明 | 覆盖范围 |
|---------|------|---------|
| `MallSearchMapperTests.java` | Search 模块测试 | Mapper + ES 数据准备 |
| `MallSearchApplicationTests.java` | 应用启动测试 | 基础配置验证 |

---

## 🚀 运行测试

### 前置条件

1. **数据库必须启动**（MySQL）
2. **数据库已初始化**（运行 `document/sql/mall.sql`）
3. **Maven 已安装**（推荐 3.6.3+）
4. **JDK 17+**

### 运行方式

#### 方式 1：在 IDE 中运行（推荐）

**IntelliJ IDEA：**

1. 打开测试文件
2. 右键点击类名或方法名
3. 选择 "Run '类名'" 或 "Debug '类名'"
4. 查看控制台输出

**常用测试类：**

- `MyBatisPlusTests` - Admin Service 层完整测试
- `MyBatisPlusMapperTests` - Admin Mapper 层测试
- `MultiModuleMapperTests` - 多模块 Mapper 测试
- `MultiModuleServiceTests` - 多模块 Service 测试
- `MyBatisPlusPortalTests` - Portal 模块测试

#### 方式 2：使用 Maven 命令

```bash
# 进入项目根目录
cd d:\A\GithubCode\mall\nova-mall

# 运行所有模块的测试
mvn test

# 运行特定模块的测试
cd nova-mall-admin
mvn test

# 运行特定测试类
mvn test -Dtest=MyBatisPlusTests

# 运行特定测试方法
mvn test -Dtest=MyBatisPlusTests#testListAllBrands
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
✅ testPageQueryBrands()        - 分页查询 + 条件查询
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
✅ testCategoryService()        - PmsProductCategoryService
✅ testBrandService()           - PmsBrandService
✅ testHomeBrandService()       - SmsHomeBrandService
✅ testCreateBrand()            - Mapper insert 新增
✅ testUpdateBrand()            - Mapper updateById 更新
✅ testComplexQuery()          - LambdaQueryWrapper 复杂查询
✅ testBatchDelete()           - LambdaQueryWrapper 批量删除
✅ testPageQuery()            - 分页查询验证
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

---

## 📈 测试预期结果

### ✅ 成功标志

1. **所有测试通过**（绿色勾选）
2. **无异常抛出**
3. **控制台输出**：
   ```
   ====== 测试1：查询所有品牌 ==========
   查询到品牌数量: 5
   第一个品牌: 小米
   ```

### ❌ 失败排查

1. **检查数据库连接**
2. **检查数据是否存在**
3. **查看详细错误日志**
4. **使用 Debug 模式调试**

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
LOGGER.info("查询到品牌数量: {}", brandList.size());
LOGGER.info("新增品牌成功，ID: {}", brand.getId());
```

### 4. 明确的断言

```java
assertNotNull(brandList, "品牌列表不应为 null");
assertEquals(1, result, "新增应该返回 1");
assertTrue(list.size() > 0, "应该至少有一个品牌");
```

---

## 📝 修改测试数据

如果需要修改测试数据，请编辑对应的 SQL 文件：

- **测试数据 SQL**：`document/sql/mall.sql`
- **测试品牌**：`pms_brand` 表
- **测试分类**：`pms_product_category` 表
- **测试优惠券**：`sms_coupon` 表

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
      - name: Run tests
        run: mvn test
```

---

## 📞 获取帮助

如果测试运行过程中遇到问题：

1. 查看详细错误日志
2. 检查数据库连接配置
3. 确认测试数据是否存在
4. 查看 [MyBatis-Plus 官方文档](https://baomidou.com/)

---

**祝测试愉快！🎉**
