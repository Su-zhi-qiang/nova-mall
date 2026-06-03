package com.su.mall;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.PmsBrandMapper;
import com.su.mall.model.PmsBrand;
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
 * MyBatis-Plus Mapper 层基础测试
 * 只测试 Mapper 层，不依赖 Service
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MyBatisPlusMapperTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisPlusMapperTests.class);

    @Autowired
    private PmsBrandMapper brandMapper;

    /**
     * 测试1：selectList 查询
     */
    @Test
    @Order(1)
    @DisplayName("测试 selectList")
    public void testSelectList() {
        LOGGER.info("========== 测试 selectList ==========");
        
        List<PmsBrand> list = brandMapper.selectList(null);
        
        assertNotNull(list);
        assertTrue(list.size() > 0);
        LOGGER.info("查询到 {} 条记录", list.size());
    }

    /**
     * 测试2：selectById 查询
     */
    @Test
    @Order(2)
    @DisplayName("测试 selectById")
    public void testSelectById() {
        LOGGER.info("========== 测试 selectById ==========");
        
        List<PmsBrand> list = brandMapper.selectList(null);
        if (!list.isEmpty()) {
            Long id = list.get(0).getId();
            
            PmsBrand brand = brandMapper.selectById(id);
            
            assertNotNull(brand);
            LOGGER.info("根据ID {} 查询到: {}", id, brand.getName());
        }
    }

    /**
     * 测试3：insert 新增
     */
    @Test
    @Order(3)
    @Transactional
    @Rollback
    @DisplayName("测试 insert")
    public void testInsert() {
        LOGGER.info("========== 测试 insert ==========");
        
        PmsBrand brand = new PmsBrand();
        brand.setName("Mapper测试品牌");
        brand.setFirstLetter("M");
        brand.setSort(100);
        brand.setFactoryStatus(0);
        brand.setShowStatus(1);
        brand.setProductCount(0);
        brand.setProductCommentCount(0);
        
        int result = brandMapper.insert(brand);
        
        assertEquals(1, result);
        assertNotNull(brand.getId());
        LOGGER.info("新增成功，ID: {}", brand.getId());
    }

    /**
     * 测试4：updateById 更新
     */
    @Test
    @Order(4)
    @Transactional
    @Rollback
    @DisplayName("测试 updateById")
    public void testUpdateById() {
        LOGGER.info("========== 测试 updateById ==========");
        
        // 先查询一个
        List<PmsBrand> list = brandMapper.selectList(null);
        if (!list.isEmpty()) {
            PmsBrand brand = list.get(0);
            String originalName = brand.getName();
            
            brand.setName(originalName + "-UPDATED");
            brand.setSort(999);
            
            int result = brandMapper.updateById(brand);
            
            assertEquals(1, result);
            
            PmsBrand updated = brandMapper.selectById(brand.getId());
            assertEquals(originalName + "-UPDATED", updated.getName());
            LOGGER.info("更新成功");
        }
    }

    /**
     * 测试5：deleteById 删除
     */
    @Test
    @Order(5)
    @Transactional
    @Rollback
    @DisplayName("测试 deleteById")
    public void testDeleteById() {
        LOGGER.info("========== 测试 deleteById ==========");
        
        // 先新增一个
        PmsBrand brand = new PmsBrand();
        brand.setName("待删除品牌");
        brand.setSort(1);
        brandMapper.insert(brand);
        
        // 然后删除
        int result = brandMapper.deleteById(brand.getId());
        
        assertEquals(1, result);
        
        PmsBrand deleted = brandMapper.selectById(brand.getId());
        assertNull(deleted);
        LOGGER.info("删除成功");
    }

    /**
     * 测试6：LambdaQueryWrapper 条件查询
     */
    @Test
    @Order(6)
    @DisplayName("测试 LambdaQueryWrapper 条件查询")
    public void testLambdaQueryWrapper() {
        LOGGER.info("========== 测试 LambdaQueryWrapper ==========");
        
        // 测试 like 查询
        LambdaQueryWrapper<PmsBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(PmsBrand::getName, "小米");
        List<PmsBrand> result = brandMapper.selectList(wrapper);
        
        LOGGER.info("关键词'小米'查询到 {} 条记录", result.size());
        
        // 测试 eq 查询
        LambdaQueryWrapper<PmsBrand> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(PmsBrand::getShowStatus, 1);
        wrapper2.orderByDesc(PmsBrand::getSort);
        List<PmsBrand> result2 = brandMapper.selectList(wrapper2);
        
        LOGGER.info("状态=1的记录数量: {}", result2.size());
    }
}
