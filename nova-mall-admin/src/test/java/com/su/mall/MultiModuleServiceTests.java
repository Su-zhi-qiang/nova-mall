package com.su.mall;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.PmsProductCategoryMapper;
import com.su.mall.mapper.PmsBrandMapper;
import com.su.mall.mapper.PmsProductMapper;
import com.su.mall.mapper.SmsHomeBrandMapper;
import com.su.mall.model.PmsProductCategory;
import com.su.mall.model.PmsBrand;
import com.su.mall.model.PmsProduct;
import com.su.mall.model.SmsHomeBrand;
import com.su.mall.service.PmsProductCategoryService;
import com.su.mall.service.PmsBrandService;
import com.su.mall.service.SmsHomeBrandService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MyBatis-Plus 多模块 Service 层测试
 * 验证不同模块的 Service 是否正常工作
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiModuleServiceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiModuleServiceTests.class);

    @Autowired
    private PmsProductCategoryService categoryService;

    @Autowired
    private PmsBrandService brandService;

    @Autowired
    private SmsHomeBrandService homeBrandService;

    @Autowired
    private PmsProductCategoryMapper categoryMapper;

    @Autowired
    private PmsBrandMapper brandMapper;

    @Autowired
    private PmsProductMapper productMapper;

    @Autowired
    private SmsHomeBrandMapper homeBrandMapper;

    /**
     * 测试1：PmsProductCategoryService - 商品分类服务
     */
    @Test
    @Order(1)
    @DisplayName("测试 PmsProductCategoryService - 商品分类服务")
    public void testCategoryService() {
        LOGGER.info("========== 测试 PmsProductCategoryService ==========");
        
        // 测试查询顶级分类
        List<PmsProductCategory> list = categoryService.getList(0L, 1, 10);
        
        assertNotNull(list, "分类列表不应为 null");
        LOGGER.info("查询到 {} 个顶级分类", list.size());
        
        // 测试根据 ID 查询单个分类
        if (!list.isEmpty()) {
            PmsProductCategory category = list.get(0);
            PmsProductCategory item = categoryService.getItem(category.getId());
            
            assertNotNull(item, "单个分类查询结果不应为 null");
            assertEquals(category.getId(), item.getId());
            LOGGER.info("查询单个分类成功: {}", item.getName());
        }
    }

    /**
     * 测试2：PmsBrandService - 商品品牌服务
     */
    @Test
    @Order(2)
    @DisplayName("测试 PmsBrandService - 商品品牌服务")
    public void testBrandService() {
        LOGGER.info("========== 测试 PmsBrandService ==========");
        
        // 测试查询所有品牌
        List<PmsBrand> allBrands = brandService.listAllBrand();
        assertNotNull(allBrands);
        LOGGER.info("查询到 {} 个品牌", allBrands.size());
        
        // 测试分页查询
        List<PmsBrand> pageBrands = brandService.listBrand("小米", null, 1, 10);
        assertNotNull(pageBrands);
        LOGGER.info("关键词'小米'查询到 {} 个品牌", pageBrands.size());
        
        // 测试根据 ID 查询
        if (!allBrands.isEmpty()) {
            PmsBrand brand = brandService.getBrand(allBrands.get(0).getId());
            assertNotNull(brand);
            LOGGER.info("查询单个品牌成功: {}", brand.getName());
        }
    }

    /**
     * 测试3：SmsHomeBrandService - 首页品牌服务
     */
    @Test
    @Order(3)
    @DisplayName("测试 SmsHomeBrandService - 首页品牌服务")
    public void testHomeBrandService() {
        LOGGER.info("========== 测试 SmsHomeBrandService ==========");
        
        // 测试查询所有推荐品牌
        List<SmsHomeBrand> allBrands = homeBrandService.list(null, null, 10, 1);
        assertNotNull(allBrands);
        LOGGER.info("查询到 {} 个首页品牌", allBrands.size());
        
        // 测试按品牌名称查询
        if (!allBrands.isEmpty()) {
            String brandName = allBrands.get(0).getBrandName();
            List<SmsHomeBrand> filteredBrands = homeBrandService.list(brandName, null, 10, 1);
            assertNotNull(filteredBrands);
            LOGGER.info("品牌名称'{}'查询到 {} 个结果", brandName, filteredBrands.size());
        }
    }

    /**
     * 测试4：新增品牌 - MyBatis-Plus insert
     */
    @Test
    @Order(4)
    @Transactional
    @Rollback
    @DisplayName("测试新增品牌 - MyBatis-Plus insert")
    public void testCreateBrand() {
        LOGGER.info("========== 测试新增品牌 ==========");
        
        // 使用 Mapper 直接插入测试
        PmsBrand brand = new PmsBrand();
        brand.setName("新测试品牌");
        brand.setLogo("http://example.com/logo.jpg");
        brand.setFirstLetter("X");
        brand.setSort(100);
        brand.setFactoryStatus(0);
        brand.setShowStatus(1);
        brand.setProductCount(0);
        brand.setProductCommentCount(0);
        
        int result = brandMapper.insert(brand);
        
        assertEquals(1, result, "插入应该返回 1");
        assertNotNull(brand.getId(), "品牌 ID 不应为 null");
        
        LOGGER.info("新增品牌成功，ID: {}", brand.getId());
        
        // 验证查询
        PmsBrand inserted = brandMapper.selectById(brand.getId());
        assertNotNull(inserted);
        assertEquals("新测试品牌", inserted.getName());
    }

    /**
     * 测试5：更新品牌 - MyBatis-Plus updateById
     */
    @Test
    @Order(5)
    @Transactional
    @Rollback
    @DisplayName("测试更新品牌 - MyBatis-Plus updateById")
    public void testUpdateBrand() {
        LOGGER.info("========== 测试更新品牌 ==========");
        
        // 先新增一个品牌
        PmsBrand brand = new PmsBrand();
        brand.setName("待更新品牌");
        brand.setLogo("http://example.com/old.jpg");
        brand.setFirstLetter("D");
        brand.setSort(1);
        brand.setFactoryStatus(0);
        brand.setShowStatus(1);
        brand.setProductCount(0);
        brand.setProductCommentCount(0);
        
        brandMapper.insert(brand);
        Long brandId = brand.getId();
        
        // 更新
        brand.setName("已更新品牌");
        brand.setLogo("http://example.com/new.jpg");
        brand.setSort(999);
        
        int result = brandMapper.updateById(brand);
        
        assertEquals(1, result, "更新应该返回 1");
        
        // 验证更新
        PmsBrand updated = brandMapper.selectById(brandId);
        assertEquals("已更新品牌", updated.getName());
        assertEquals("http://example.com/new.jpg", updated.getLogo());
        assertEquals(999, updated.getSort());
        
        LOGGER.info("更新品牌成功");
    }

    /**
     * 测试6：LambdaQueryWrapper 复杂条件查询
     */
    @Test
    @Order(6)
    @DisplayName("测试 LambdaQueryWrapper 复杂条件查询")
    public void testComplexQuery() {
        LOGGER.info("========== 测试 LambdaQueryWrapper 复杂条件查询 ==========");
        
        // 测试多条件查询
        LambdaQueryWrapper<PmsBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(PmsBrand::getName, "小米")
              .or()
              .eq(PmsBrand::getShowStatus, 1)
              .orderByDesc(PmsBrand::getSort)
              .last("LIMIT 10");
        
        List<PmsBrand> brands = brandMapper.selectList(wrapper);
        
        assertNotNull(brands);
        LOGGER.info("复杂条件查询到 {} 个品牌", brands.size());
        
        // 测试 IN 查询
        if (brands.size() >= 2) {
            List<Long> ids = brands.subList(0, Math.min(2, brands.size()))
                .stream()
                .map(PmsBrand::getId)
                .toList();
            
            LambdaQueryWrapper<PmsBrand> inWrapper = new LambdaQueryWrapper<>();
            inWrapper.in(PmsBrand::getId, ids);
            
            List<PmsBrand> inBrands = brandMapper.selectList(inWrapper);
            
            assertNotNull(inBrands);
            assertEquals(ids.size(), inBrands.size());
            LOGGER.info("IN 查询成功，查询到 {} 个品牌", inBrands.size());
        }
    }

    /**
     * 测试7：批量删除
     */
    @Test
    @Order(7)
    @Transactional
    @Rollback
    @DisplayName("测试批量删除")
    public void testBatchDelete() {
        LOGGER.info("========== 测试批量删除 ==========");
        
        // 新增两个测试品牌
        PmsBrand brand1 = new PmsBrand();
        brand1.setName("待删除品牌1");
        brand1.setSort(1);
        brand1.setFactoryStatus(0);
        brand1.setShowStatus(1);
        brand1.setProductCount(0);
        brand1.setProductCommentCount(0);
        brandMapper.insert(brand1);
        
        PmsBrand brand2 = new PmsBrand();
        brand2.setName("待删除品牌2");
        brand2.setSort(2);
        brand2.setFactoryStatus(0);
        brand2.setShowStatus(1);
        brand2.setProductCount(0);
        brand2.setProductCommentCount(0);
        brandMapper.insert(brand2);
        
        List<Long> ids = List.of(brand1.getId(), brand2.getId());
        
        // 批量删除
        LambdaQueryWrapper<PmsBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PmsBrand::getId, ids);
        
        int result = brandMapper.delete(wrapper);
        
        assertEquals(2, result, "应该删除 2 条记录");
        
        // 验证删除
        PmsBrand deleted1 = brandMapper.selectById(brand1.getId());
        PmsBrand deleted2 = brandMapper.selectById(brand2.getId());
        
        assertNull(deleted1, "品牌1应该已被删除");
        assertNull(deleted2, "品牌2应该已被删除");
        
        LOGGER.info("批量删除成功");
    }

    /**
     * 测试8：分页查询
     */
    @Test
    @Order(8)
    @DisplayName("测试分页查询")
    public void testPageQuery() {
        LOGGER.info("========== 测试分页查询 ==========");
        
        // 测试商品分类分页查询
        List<PmsProductCategory> allCategories = categoryService.getList(0L, 1, 100);
        assertNotNull(allCategories);
        int totalCount = allCategories.size();
        LOGGER.info("数据库总共有 {} 个一级分类", totalCount);
        
        // 测试首页品牌分页
        List<SmsHomeBrand> brandPage1 = homeBrandService.list(null, null, 3, 1);
        assertNotNull(brandPage1);
        LOGGER.info("首页品牌第1页查询到 {} 个", brandPage1.size());
    }

    /**
     * 测试总结
     */
    @AfterAll
    static void afterAll() {
        LOGGER.info("========== 多模块 Service 测试完成 ==========");
    }
}
