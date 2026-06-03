package com.su.mall;

import cn.hutool.core.collection.CollUtil;
import com.su.mall.dto.PmsBrandParam;
import com.su.mall.mapper.PmsBrandMapper;
import com.su.mall.model.PmsBrand;
import com.su.mall.service.PmsBrandService;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MyBatis-Plus 改造后功能测试
 * 重点测试 CRUD 操作是否正常
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MyBatisPlusTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisPlusTests.class);

    @Autowired
    private PmsBrandMapper brandMapper;

    @Autowired
    private PmsBrandService brandService;

    private static Long testBrandId;

    /**
     * 测试1：查询所有品牌
     * 验证 selectList 是否正常
     */
    @Test
    @Order(1)
    @DisplayName("测试查询所有品牌")
    public void testListAllBrands() {
        LOGGER.info("========== 测试1：查询所有品牌 ==========");
        
        List<PmsBrand> brandList = brandService.listAllBrand();
        
        assertNotNull(brandList, "品牌列表不应为null");
        assertTrue(brandList.size() > 0, "应该至少有一个品牌");
        
        LOGGER.info("查询到品牌数量: {}", brandList.size());
        LOGGER.info("第一个品牌: {}", brandList.get(0).getName());
    }

    /**
     * 测试2：分页查询品牌
     * 验证分页和条件查询是否正常
     */
    @Test
    @Order(2)
    @DisplayName("测试分页查询品牌")
    public void testPageQueryBrands() {
        LOGGER.info("========== 测试2：分页查询品牌 ==========");
        
        // 测试空条件查询
        List<PmsBrand> brandList1 = brandService.listBrand(null, null, 1, 10);
        assertNotNull(brandList1);
        
        // 测试关键词查询
        List<PmsBrand> brandList2 = brandService.listBrand("小米", null, 1, 10);
        assertNotNull(brandList2);
        LOGGER.info("关键词'小米'查询结果数量: {}", brandList2.size());
        
        // 测试状态查询
        List<PmsBrand> brandList3 = brandService.listBrand(null, 1, 1, 10);
        assertNotNull(brandList3);
        LOGGER.info("显示状态为1的品牌数量: {}", brandList3.size());
    }

    /**
     * 测试3：新增品牌
     * 验证 insert 是否正常
     */
    @Test
    @Order(3)
    @Transactional
    @Rollback
    @DisplayName("测试新增品牌")
    public void testCreateBrand() {
        LOGGER.info("========== 测试3：新增品牌 ==========");
        
        PmsBrandParam brandParam = new PmsBrandParam();
        brandParam.setName("测试品牌-MP");
        brandParam.setLogo("http://example.com/test-logo.jpg");
        brandParam.setFirstLetter("T");
        brandParam.setSort(100);
        brandParam.setFactoryStatus(0);
        brandParam.setShowStatus(1);
        brandParam.setBrandStory("这是一个测试品牌");
        
        int result = brandService.createBrand(brandParam);
        
        assertEquals(1, result, "新增应该返回1");
        
        // 查询新增的品牌
        List<PmsBrand> allBrands = brandService.listAllBrand();
        PmsBrand newBrand = allBrands.stream()
                .filter(b -> "测试品牌-MP".equals(b.getName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(newBrand, "应该能找到新增的品牌");
        testBrandId = newBrand.getId();
        LOGGER.info("新增品牌成功，ID: {}, 名称: {}", newBrand.getId(), newBrand.getName());
    }

    /**
     * 测试4：根据ID查询单个品牌
     * 验证 selectById 是否正常
     */
    @Test
    @Order(4)
    @DisplayName("测试根据ID查询品牌")
    public void testGetBrandById() {
        LOGGER.info("========== 测试4：根据ID查询品牌 ==========");
        
        // 先查询所有品牌获取一个存在的ID
        List<PmsBrand> brandList = brandService.listAllBrand();
        if (CollUtil.isNotEmpty(brandList)) {
            Long existId = brandList.get(0).getId();
            
            PmsBrand brand = brandService.getBrand(existId);
            
            assertNotNull(brand, "查询的品牌不应为null");
            LOGGER.info("根据ID {} 查询到品牌: {}", existId, brand.getName());
        }
    }

    /**
     * 测试5：更新品牌
     * 验证 updateById 是否正常
     */
    @Test
    @Order(5)
    @Transactional
    @Rollback
    @DisplayName("测试更新品牌")
    public void testUpdateBrand() {
        LOGGER.info("========== 测试5：更新品牌 ==========");
        
        // 先查询一个现有品牌
        List<PmsBrand> brandList = brandService.listAllBrand();
        if (CollUtil.isNotEmpty(brandList)) {
            PmsBrand originalBrand = brandList.get(0);
            Long brandId = originalBrand.getId();
            String originalName = originalBrand.getName();
            
            // 更新名称
            PmsBrandParam updateParam = new PmsBrandParam();
            updateParam.setName(originalName + "-UPDATED");
            updateParam.setLogo(originalBrand.getLogo());
            updateParam.setSort(999);
            updateParam.setFactoryStatus(originalBrand.getFactoryStatus());
            updateParam.setShowStatus(originalBrand.getShowStatus());
            
            int result = brandService.updateBrand(brandId, updateParam);
            
            assertEquals(1, result, "更新应该返回1");
            
            // 验证更新是否成功
            PmsBrand updatedBrand = brandService.getBrand(brandId);
            assertEquals(originalName + "-UPDATED", updatedBrand.getName());
            assertEquals(999, updatedBrand.getSort());
            
            LOGGER.info("更新品牌成功，ID: {}, 新名称: {}", brandId, updatedBrand.getName());
        }
    }

    /**
     * 测试6：批量删除品牌
     * 验证批量删除是否正常
     */
    @Test
    @Order(6)
    @Transactional
    @Rollback
    @DisplayName("测试批量删除品牌")
    public void testDeleteBrand() {
        LOGGER.info("========== 测试6：批量删除品牌 ==========");
        
        // 先通过Mapper创建两个测试品牌
        PmsBrand brand1 = new PmsBrand();
        brand1.setName("测试删除-1");
        brand1.setLogo("http://test.com/logo1.jpg");
        brand1.setSort(1);
        brand1.setFactoryStatus(0);
        brand1.setShowStatus(1);
        brand1.setProductCount(0);
        brand1.setProductCommentCount(0);
        brandMapper.insert(brand1);
        
        PmsBrand brand2 = new PmsBrand();
        brand2.setName("测试删除-2");
        brand2.setLogo("http://test.com/logo2.jpg");
        brand2.setSort(2);
        brand2.setFactoryStatus(0);
        brand2.setShowStatus(1);
        brand2.setProductCount(0);
        brand2.setProductCommentCount(0);
        brandMapper.insert(brand2);
        
        // 批量删除
        int result = brandService.deleteBrand(CollUtil.newArrayList(brand1.getId(), brand2.getId()));
        
        assertEquals(2, result, "应该删除2条记录");
        
        // 验证删除
        PmsBrand deleted1 = brandMapper.selectById(brand1.getId());
        PmsBrand deleted2 = brandMapper.selectById(brand2.getId());
        
        assertNull(deleted1, "品牌1应该已被删除");
        assertNull(deleted2, "品牌2应该已被删除");
        
        LOGGER.info("批量删除成功，删除数量: {}", result);
    }

    /**
     * 测试7：批量更新显示状态
     * 验证批量更新是否正常
     */
    @Test
    @Order(7)
    @Transactional
    @Rollback
    @DisplayName("测试批量更新显示状态")
    public void testUpdateShowStatus() {
        LOGGER.info("========== 测试7：批量更新显示状态 ==========");
        
        // 先查询几个品牌
        List<PmsBrand> brandList = brandService.listBrand(null, null, 1, 5);
        if (CollUtil.isNotEmpty(brandList)) {
            List<Long> ids = CollUtil.map(brandList, PmsBrand::getId, true);
            
            // 批量更新为0
            int result = brandService.updateShowStatus(ids, 0);
            
            assertEquals(ids.size(), result, "应该更新" + ids.size() + "条记录");
            
            // 验证更新
            for (Long id : ids) {
                PmsBrand brand = brandMapper.selectById(id);
                assertEquals(0, brand.getShowStatus());
            }
            
            LOGGER.info("批量更新显示状态成功，影响数量: {}", result);
        }
    }

    /**
     * 测试8：批量更新厂家状态
     * 验证批量更新是否正常
     */
    @Test
    @Order(8)
    @Transactional
    @Rollback
    @DisplayName("测试批量更新厂家状态")
    public void testUpdateFactoryStatus() {
        LOGGER.info("========== 测试8：批量更新厂家状态 ==========");
        
        List<PmsBrand> brandList = brandService.listBrand(null, null, 1, 5);
        if (CollUtil.isNotEmpty(brandList)) {
            List<Long> ids = CollUtil.map(brandList, PmsBrand::getId, true);
            
            int result = brandService.updateFactoryStatus(ids, 1);
            
            assertEquals(ids.size(), result);
            
            for (Long id : ids) {
                PmsBrand brand = brandMapper.selectById(id);
                assertEquals(1, brand.getFactoryStatus());
            }
            
            LOGGER.info("批量更新厂家状态成功，影响数量: {}", result);
        }
    }

    /**
     * 测试总结
     */
    @AfterAll
    static void afterAll() {
        LOGGER.info("========== MyBatis-Plus 功能测试完成 ==========");
    }
}
