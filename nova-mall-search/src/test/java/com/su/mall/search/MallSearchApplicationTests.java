package com.su.mall.search;

import com.su.mall.search.dao.EsProductDao;
import com.su.mall.search.domain.EsProduct;
import com.su.mall.search.service.impl.EsClientService;
import com.su.mall.search.service.impl.EsProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Search模块测试
 */
@SpringBootTest
public class MallSearchApplicationTests {
    
    @Autowired
    private EsProductDao productDao;
    
    @Autowired
    private EsProductServiceImpl esProductService;
    
    @Autowired
    private EsClientService esClientService;
    
    @Test
    public void contextLoads() {
        System.out.println("Search模块测试启动成功");
    }
    
    @Test
    public void testGetAllEsProductList() {
        List<EsProduct> esProductList = productDao.getAllEsProductList(null);
        System.out.println("查询到 " + esProductList.size() + " 个商品");
    }
    
    @Test
    public void testEsClientService() {
        System.out.println("测试ES客户端服务");
        assert esClientService != null;
    }
    
    @Test
    public void testSearch() {
        System.out.println("测试搜索功能");
        Page<EsProduct> result = esProductService.search("手机", 0, 10);
        System.out.println("搜索结果: " + result.getTotalElements() + " 条");
    }
}
