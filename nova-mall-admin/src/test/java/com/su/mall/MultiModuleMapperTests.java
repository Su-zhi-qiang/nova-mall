package com.su.mall;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.su.mall.mapper.PmsProductCategoryMapper;
import com.su.mall.mapper.PmsSkuStockMapper;
import com.su.mall.mapper.SmsHomeBrandMapper;
import com.su.mall.mapper.SmsCouponMapper;
import com.su.mall.mapper.UmsMenuMapper;
import com.su.mall.mapper.UmsAdminMapper;
import com.su.mall.model.PmsProductCategory;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.model.SmsHomeBrand;
import com.su.mall.model.SmsCoupon;
import com.su.mall.model.UmsMenu;
import com.su.mall.model.UmsAdmin;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MyBatis-Plus 多模块 Mapper 测试
 * 验证不同模块的 Mapper 是否正常工作
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiModuleMapperTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiModuleMapperTests.class);

    @Autowired
    private PmsProductCategoryMapper categoryMapper;

    @Autowired
    private PmsSkuStockMapper skuStockMapper;

    @Autowired
    private SmsHomeBrandMapper homeBrandMapper;

    @Autowired
    private SmsCouponMapper couponMapper;

    @Autowired
    private UmsMenuMapper menuMapper;

    @Autowired
    private UmsAdminMapper adminMapper;

    /**
     * 测试1：PmsProductCategoryMapper - 商品分类
     */
    @Test
    @Order(1)
    @DisplayName("测试 PmsProductCategoryMapper - 商品分类")
    public void testCategoryMapper() {
        LOGGER.info("========== 测试 PmsProductCategoryMapper ==========");
        
        List<PmsProductCategory> list = categoryMapper.selectList(null);
        
        assertNotNull(list, "分类列表不应为 null");
        LOGGER.info("查询到 {} 个商品分类", list.size());
        
        // 测试 LambdaQueryWrapper
        LambdaQueryWrapper<PmsProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductCategory::getParentId, 0)
              .orderByDesc(PmsProductCategory::getSort);
        List<PmsProductCategory> topCategories = categoryMapper.selectList(wrapper);
        
        assertNotNull(topCategories);
        LOGGER.info("查询到 {} 个顶级分类", topCategories.size());
    }

    /**
     * 测试2：PmsSkuStockMapper - SKU库存
     */
    @Test
    @Order(2)
    @DisplayName("测试 PmsSkuStockMapper - SKU库存")
    public void testSkuStockMapper() {
        LOGGER.info("========== 测试 PmsSkuStockMapper ==========");
        
        List<PmsSkuStock> list = skuStockMapper.selectList(null);
        
        assertNotNull(list);
        LOGGER.info("查询到 {} 个 SKU 记录", list.size());
        
        // 测试条件查询
        if (!list.isEmpty()) {
            PmsSkuStock firstSku = list.get(0);
            LambdaQueryWrapper<PmsSkuStock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PmsSkuStock::getProductId, firstSku.getProductId());
            List<PmsSkuStock> skuList = skuStockMapper.selectList(wrapper);
            
            assertNotNull(skuList);
            assertTrue(!skuList.isEmpty());
            LOGGER.info("商品 ID {} 有 {} 个 SKU", firstSku.getProductId(), skuList.size());
        }
    }

    /**
     * 测试3：SmsHomeBrandMapper - 首页品牌
     */
    @Test
    @Order(3)
    @DisplayName("测试 SmsHomeBrandMapper - 首页品牌")
    public void testHomeBrandMapper() {
        LOGGER.info("========== 测试 SmsHomeBrandMapper ==========");
        
        List<SmsHomeBrand> list = homeBrandMapper.selectList(null);
        
        assertNotNull(list);
        LOGGER.info("查询到 {} 个首页品牌", list.size());
        
        // 测试按状态查询
        LambdaQueryWrapper<SmsHomeBrand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsHomeBrand::getRecommendStatus, 1)
              .orderByDesc(SmsHomeBrand::getSort);
        List<SmsHomeBrand> recommendBrands = homeBrandMapper.selectList(wrapper);
        
        assertNotNull(recommendBrands);
        LOGGER.info("查询到 {} 个推荐品牌", recommendBrands.size());
    }

    /**
     * 测试4：SmsCouponMapper - 优惠券
     */
    @Test
    @Order(4)
    @DisplayName("测试 SmsCouponMapper - 优惠券")
    public void testCouponMapper() {
        LOGGER.info("========== 测试 SmsCouponMapper ==========");
        
        List<SmsCoupon> list = couponMapper.selectList(null);
        
        assertNotNull(list);
        LOGGER.info("查询到 {} 个优惠券", list.size());
        
        // 测试新增
        SmsCoupon coupon = new SmsCoupon();
        coupon.setName("测试优惠券-MP");
        coupon.setType(0);
        coupon.setCount(100);
        coupon.setUseCount(0);
        coupon.setReceiveCount(0);
        coupon.setPublishCount(100);
        coupon.setMinPoint(new BigDecimal("100"));
        coupon.setAmount(new BigDecimal("10.0"));
        
        int result = couponMapper.insert(coupon);
        assertEquals(1, result, "新增优惠券应该返回 1");
        assertNotNull(coupon.getId(), "优惠券 ID 不应为 null");
        
        LOGGER.info("新增优惠券成功，ID: {}", coupon.getId());
        
        // 测试按类型查询
        LambdaQueryWrapper<SmsCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsCoupon::getType, 0);
        List<SmsCoupon> typeList = couponMapper.selectList(wrapper);
        assertNotNull(typeList);
        LOGGER.info("类型 0 的优惠券数量: {}", typeList.size());
    }

    /**
     * 测试5：UmsMenuMapper - 后台菜单
     */
    @Test
    @Order(5)
    @DisplayName("测试 UmsMenuMapper - 后台菜单")
    public void testMenuMapper() {
        LOGGER.info("========== 测试 UmsMenuMapper ==========");
        
        List<UmsMenu> list = menuMapper.selectList(null);
        
        assertNotNull(list);
        LOGGER.info("查询到 {} 个菜单", list.size());
        
        // 测试按父ID查询
        LambdaQueryWrapper<UmsMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsMenu::getParentId, 0)
              .orderByAsc(UmsMenu::getSort);
        List<UmsMenu> topMenus = menuMapper.selectList(wrapper);
        
        assertNotNull(topMenus);
        LOGGER.info("查询到 {} 个顶级菜单", topMenus.size());
    }

    /**
     * 测试6：UmsAdminMapper - 后台管理员
     */
    @Test
    @Order(6)
    @DisplayName("测试 UmsAdminMapper - 后台管理员")
    public void testAdminMapper() {
        LOGGER.info("========== 测试 UmsAdminMapper ==========");
        
        List<UmsAdmin> list = adminMapper.selectList(null);
        
        assertNotNull(list);
        LOGGER.info("查询到 {} 个管理员", list.size());
        
        // 测试按用户名查询
        if (!list.isEmpty()) {
            UmsAdmin admin = list.get(0);
            LambdaQueryWrapper<UmsAdmin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UmsAdmin::getUsername, admin.getUsername());
            UmsAdmin found = adminMapper.selectOne(wrapper);
            
            assertNotNull(found);
            assertEquals(admin.getUsername(), found.getUsername());
            LOGGER.info("根据用户名查询管理员成功: {}", found.getUsername());
        }
    }

    /**
     * 测试7：LambdaUpdateWrapper - 批量更新
     */
    @Test
    @Order(7)
    @Transactional
    @Rollback
    @DisplayName("测试 LambdaUpdateWrapper - 批量更新")
    public void testLambdaUpdateWrapper() {
        LOGGER.info("========== 测试 LambdaUpdateWrapper 批量更新 ==========");
        
        // 查询几个分类
        LambdaQueryWrapper<PmsProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PmsProductCategory::getId).last("LIMIT 3");
        List<PmsProductCategory> categories = categoryMapper.selectList(queryWrapper);
        
        if (!categories.isEmpty()) {
            List<Long> ids = categories.stream()
                .map(PmsProductCategory::getId)
                .toList();
            
            // 批量更新
            PmsProductCategory update = new PmsProductCategory();
            update.setNavStatus(1);
            
            LambdaUpdateWrapper<PmsProductCategory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(PmsProductCategory::getId, ids);
            
            int result = categoryMapper.update(update, updateWrapper);
            
            assertEquals(categories.size(), result, "更新数量应该匹配");
            LOGGER.info("批量更新了 {} 个分类", result);
        }
    }

    /**
     * 测试8：deleteById - 删除操作
     */
    @Test
    @Order(8)
    @Transactional
    @Rollback
    @DisplayName("测试 deleteById - 删除操作")
    public void testDeleteById() {
        LOGGER.info("========== 测试 deleteById 删除操作 ==========");
        
        // 新增一个测试优惠券
        SmsCoupon coupon = new SmsCoupon();
        coupon.setName("待删除优惠券");
        coupon.setType(0);
        coupon.setCount(10);
        coupon.setUseCount(0);
        coupon.setReceiveCount(0);
        coupon.setPublishCount(10);
        coupon.setMinPoint(new BigDecimal("0"));
        coupon.setAmount(new BigDecimal("5.0"));
        
        couponMapper.insert(coupon);
        Long couponId = coupon.getId();
        
        // 删除
        int result = couponMapper.deleteById(couponId);
        
        assertEquals(1, result, "删除应该返回 1");
        
        // 验证删除
        SmsCoupon deleted = couponMapper.selectById(couponId);
        assertNull(deleted, "删除后应该找不到记录");
        
        LOGGER.info("删除优惠券成功，ID: {}", couponId);
    }

    /**
     * 测试总结
     */
    @AfterAll
    static void afterAll() {
        LOGGER.info("========== 多模块 Mapper 测试完成 ==========");
    }
}
