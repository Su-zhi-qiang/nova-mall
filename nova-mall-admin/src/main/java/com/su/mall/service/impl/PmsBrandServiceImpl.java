package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dto.PmsBrandParam;
import com.su.mall.mapper.PmsBrandMapper;
import com.su.mall.mapper.PmsProductMapper;
import com.su.mall.model.PmsBrand;
import com.su.mall.model.PmsProduct;
import com.su.mall.service.PmsBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品品牌管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class PmsBrandServiceImpl implements PmsBrandService {
    private final PmsBrandMapper brandMapper;
    private final PmsProductMapper productMapper;

    @Override
    public List<PmsBrand> listAllBrand() {
        // ✅ 改造：selectList 替代 selectByExample
        return brandMapper.selectList(new LambdaQueryWrapper<PmsBrand>());
    }

    @Override
    public int createBrand(PmsBrandParam pmsBrandParam) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandParam, pmsBrand);
        //如果创建时首字母为空，取名称的第一个为首字母
        if (StrUtil.isEmpty(pmsBrand.getFirstLetter())) {
            pmsBrand.setFirstLetter(pmsBrand.getName().substring(0, 1));
        }
        // ✅ 改造：insert 替代 insertSelective
        return brandMapper.insert(pmsBrand);
    }

    @Override
    public int updateBrand(Long id, PmsBrandParam pmsBrandParam) {
        PmsBrand pmsBrand = new PmsBrand();
        BeanUtils.copyProperties(pmsBrandParam, pmsBrand);
        pmsBrand.setId(id);
        //如果创建时首字母为空，取名称的第一个为首字母
        if (StrUtil.isEmpty(pmsBrand.getFirstLetter())) {
            pmsBrand.setFirstLetter(pmsBrand.getName().substring(0, 1));
        }
        //更新品牌时要更新商品中的品牌名称
        PmsProduct product = new PmsProduct();
        product.setBrandName(pmsBrand.getName());
        // ✅ 改造：LambdaQueryWrapper 替代 Example
        LambdaQueryWrapper<PmsProduct> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(PmsProduct::getBrandId, id);
        productMapper.update(product, productWrapper);
        // ✅ 改造：updateById 替代 updateByPrimaryKeySelective
        return brandMapper.updateById(pmsBrand);
    }

    @Override
    public int deleteBrand(Long id) {
        // ✅ 改造：deleteById 替代 deleteByPrimaryKey
        return brandMapper.deleteById(id);
    }

    @Override
    public int deleteBrand(List<Long> ids) {
        // ✅ 改造：delete 替代 deleteByExample
        return brandMapper.delete(new LambdaQueryWrapper<PmsBrand>().in(PmsBrand::getId, ids));
    }

    @Override
    public Page<PmsBrand> listBrand(String keyword, Integer showStatus, int pageNum, int pageSize) {
        Page<PmsBrand> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：LambdaQueryWrapper 替代 Example
        return brandMapper.selectPage(page,
            new LambdaQueryWrapper<PmsBrand>()
                .like(!StrUtil.isEmpty(keyword), PmsBrand::getName, keyword)
                .eq(showStatus != null, PmsBrand::getShowStatus, showStatus)
                .orderByDesc(PmsBrand::getSort)
        );
    }

    @Override
    public PmsBrand getBrand(Long id) {
        // ✅ 改造：selectById 替代 selectByPrimaryKey
        return brandMapper.selectById(id);
    }

    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        // ✅ 改造：update + LambdaQueryWrapper 替代 updateByExampleSelective
        PmsBrand brand = new PmsBrand();
        brand.setShowStatus(showStatus);
        return brandMapper.update(brand, new LambdaQueryWrapper<PmsBrand>().in(PmsBrand::getId, ids));
    }

    @Override
    public int updateFactoryStatus(List<Long> ids, Integer factoryStatus) {
        // ✅ 改造：update + LambdaQueryWrapper 替代 updateByExampleSelective
        PmsBrand brand = new PmsBrand();
        brand.setFactoryStatus(factoryStatus);
        return brandMapper.update(brand, new LambdaQueryWrapper<PmsBrand>().in(PmsBrand::getId, ids));
    }
}
