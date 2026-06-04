package com.su.mall.portal;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.mapper.PmsBrandMapper;
import com.su.mall.model.PmsBrand;
import com.su.mall.model.PmsProduct;
import com.su.mall.portal.service.PmsPortalBrandService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MyBatis-Plus 改造后 Portal 模块功能测试
 * 重点测试 CRUD 操作是否正常
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MyBatisPlusPortalTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisPlusPortalTests.class);

    @Autowired
    private PmsBrandMapper brandMapper;

    @Autowired
    private PmsPortalBrandService portalBrandService;

    /**
     * 测试1：查询推荐品牌列表
     * 验证 selectList 是否正常
     */
    @Test
    @Order(1)
    @DisplayName("测试 Portal 查询推荐品牌")
    public void testRecommendListBrands() {
        LOGGER.info("========== 测试1：Portal 查询推荐品牌 ==========");
        
        // pageNum 应该从 1 开始，而不是 0
        Page<PmsBrand> brandPage = portalBrandService.list(1, 10);
        
        assertNotNull(brandPage, "品牌分页对象不应为 null");
        
        LOGGER.info("查询到品牌数量: {}", brandPage.getRecords().size());
        LOGGER.info("总记录数: {}", brandPage.getTotal());
        
        if (CollUtil.isNotEmpty(brandPage.getRecords())) {
            LOGGER.info("第一个品牌: {}", brandPage.getRecords().get(0).getName());
        }
        
        // 测试 CommonPage 转换
        CommonPage<PmsBrand> commonPage = CommonPage.restPage(brandPage);
        assertNotNull(commonPage);
        LOGGER.info("CommonPage 转换成功，总页数: {}, 当前页: {}", commonPage.getTotalPage(), commonPage.getPageNum());
    }

    /**
     * 测试2：查询品牌详情
     * 验证 selectById 是否正常
     */
    @Test
    @Order(2)
    @DisplayName("测试 Portal 查询品牌详情")
    public void testGetBrandDetail() {
        LOGGER.info("========== 测试2：Portal 查询品牌详情 ==========");
        
        // 先通过 mapper 查询所有品牌获取一个存在的 ID
        List<PmsBrand> brandList = brandMapper.selectList(null);
        if (CollUtil.isNotEmpty(brandList)) {
            Long existId = brandList.get(0).getId();
            
            PmsBrand brand = portalBrandService.detail(existId);
            
            assertNotNull(brand, "查询的品牌不应为 null");
            LOGGER.info("根据 ID {} 查询到品牌: {}", existId, brand.getName());
        }
    }

    /**
     * 测试3：Mapper 层基础查询
     * 验证 MyBatis-Plus BaseMapper 是否正常
     */
    @Test
    @Order(3)
    @DisplayName("测试 Portal Mapper 层基础查询")
    public void testMapperBasicQuery() {
        LOGGER.info("========== 测试3：Portal Mapper 层基础查询 ==========");
        
        // 测试 selectList
        List<PmsBrand> brandList = brandMapper.selectList(null);
        assertNotNull(brandList);
        LOGGER.info("selectList 查询到 {} 条记录", brandList.size());
        
        // 测试 selectById
        if (!brandList.isEmpty()) {
            Long id = brandList.get(0).getId();
            PmsBrand brand = brandMapper.selectById(id);
            assertNotNull(brand);
            LOGGER.info("selectById 查询成功: {}", brand.getName());
        }
    }

    /**
     * 测试4：查询品牌关联商品
     * 验证分页查询品牌关联商品
     */
    @Test
    @Order(4)
    @DisplayName("测试 Portal 查询品牌关联商品")
    public void testProductList() {
        LOGGER.info("========== 测试4：Portal 查询品牌关联商品 ==========");
        
        // 先查询一个存在的品牌 ID
        List<PmsBrand> brandList = brandMapper.selectList(null);
        if (CollUtil.isNotEmpty(brandList)) {
            Long brandId = brandList.get(0).getId();
            
            Page<PmsProduct> productPage = portalBrandService.productList(brandId, 1, 10);
            assertNotNull(productPage);
            LOGGER.info("品牌 ID {} 关联商品数量: {}, 总记录数: {}", brandId, productPage.getRecords().size(), productPage.getTotal());
            
            if (CollUtil.isNotEmpty(productPage.getRecords())) {
                LOGGER.info("第一个商品: {}", productPage.getRecords().get(0).getName());
            }
        }
    }

    /**
     * 测试总结
     */
    @AfterAll
    static void afterAll() {
        LOGGER.info("========== Portal 模块 MyBatis-Plus 功能测试完成 ==========");
    }
}
