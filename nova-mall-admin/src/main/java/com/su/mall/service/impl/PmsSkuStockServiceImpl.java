package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.dao.PmsSkuStockDao;
import com.su.mall.mapper.PmsSkuStockMapper;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.service.PmsSkuStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品SKU库存管理Service实现类
 * 
 */
@Service
@RequiredArgsConstructor
public class PmsSkuStockServiceImpl implements PmsSkuStockService {
    private final PmsSkuStockMapper skuStockMapper;
    private final PmsSkuStockDao skuStockDao;

    @Override
    public List<PmsSkuStock> getList(Long pid, String keyword) {
        LambdaQueryWrapper<PmsSkuStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsSkuStock::getProductId, pid);
        if (!StrUtil.isEmpty(keyword)) {
            wrapper.like(PmsSkuStock::getSkuCode, keyword);
        }
        return skuStockMapper.selectList(wrapper);
    }

    @Override
    public int update(Long pid, List<PmsSkuStock> skuStockList) {
        List<PmsSkuStock> filterSkuList = skuStockList.stream()
                .filter(item -> pid.equals(item.getProductId()))
                .collect(Collectors.toList());
        return skuStockDao.replaceList(filterSkuList);
    }
}
