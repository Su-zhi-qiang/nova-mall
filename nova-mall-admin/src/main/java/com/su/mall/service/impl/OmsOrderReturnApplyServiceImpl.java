package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dao.OmsOrderReturnApplyDao;
import com.su.mall.dto.OmsOrderReturnApplyResult;
import com.su.mall.dto.OmsReturnApplyQueryParam;
import com.su.mall.dto.OmsUpdateStatusParam;
import com.su.mall.mapper.OmsOrderItemMapper;
import com.su.mall.mapper.OmsOrderReturnApplyMapper;
import com.su.mall.mapper.PmsProductMapper;
import com.su.mall.mapper.PmsSkuStockMapper;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.OmsOrderReturnApply;
import com.su.mall.model.PmsProduct;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.service.OmsOrderReturnApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 订单退货管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class OmsOrderReturnApplyServiceImpl implements OmsOrderReturnApplyService {
    private final OmsOrderReturnApplyDao returnApplyDao;
    private final OmsOrderReturnApplyMapper returnApplyMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final PmsSkuStockMapper skuStockMapper;
    private final PmsProductMapper productMapper;

    @Override
    public Page<OmsOrderReturnApply> list(OmsReturnApplyQueryParam queryParam, Integer pageSize, Integer pageNum) {
        Page<OmsOrderReturnApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OmsOrderReturnApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OmsOrderReturnApply::getCreateTime);
        return returnApplyMapper.selectPage(page, wrapper);
    }

    @Override
    public int delete(List<Long> ids) {
        return returnApplyMapper.delete(new LambdaQueryWrapper<OmsOrderReturnApply>()
                .eq(OmsOrderReturnApply::getStatus, 3)
                .in(OmsOrderReturnApply::getId, ids));
    }

    @Override
    @Transactional
    public int updateStatus(Long id, OmsUpdateStatusParam statusParam) {
        Integer status = statusParam.getStatus();
        OmsOrderReturnApply returnApply = new OmsOrderReturnApply();
        if(status.equals(1)){
            returnApply.setId(id);
            returnApply.setStatus(1);
            returnApply.setReturnAmount(statusParam.getReturnAmount());
            returnApply.setCompanyAddressId(statusParam.getCompanyAddressId());
            returnApply.setHandleTime(new Date());
            returnApply.setHandleMan(statusParam.getHandleMan());
            returnApply.setHandleNote(statusParam.getHandleNote());
        }else if(status.equals(2)){
            returnApply.setId(id);
            returnApply.setStatus(2);
            returnApply.setReceiveTime(new Date());
            returnApply.setReceiveMan(statusParam.getReceiveMan());
            returnApply.setReceiveNote(statusParam.getReceiveNote());
            restoreStock(id);
        }else if(status.equals(3)){
            returnApply.setId(id);
            returnApply.setStatus(3);
            returnApply.setHandleTime(new Date());
            returnApply.setHandleMan(statusParam.getHandleMan());
            returnApply.setHandleNote(statusParam.getHandleNote());
        }else{
            return 0;
        }
        return returnApplyMapper.updateById(returnApply);
    }

    /**
     * 退货完成后恢复库存（普通商品SKU库存 + 商品表库存）
     */
    private void restoreStock(Long returnApplyId) {
        OmsOrderReturnApply returnApply = returnApplyMapper.selectById(returnApplyId);
        if (returnApply == null || returnApply.getOrderId() == null) {
            return;
        }
        List<OmsOrderItem> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OmsOrderItem>()
                .eq(OmsOrderItem::getOrderId, returnApply.getOrderId()));
        if (CollectionUtils.isEmpty(orderItems)) {
            return;
        }
        for (OmsOrderItem item : orderItems) {
            // 恢复SKU库存
            if (item.getProductSkuId() != null) {
                PmsSkuStock skuStock = skuStockMapper.selectById(item.getProductSkuId());
                if (skuStock != null) {
                    skuStock.setStock(skuStock.getStock() + item.getProductQuantity());
                    skuStockMapper.updateById(skuStock);
                }
            }
            // 恢复商品表库存
            if (item.getProductId() != null) {
                PmsProduct product = productMapper.selectById(item.getProductId());
                if (product != null && product.getStock() != null) {
                    product.setStock(product.getStock() + item.getProductQuantity());
                    productMapper.updateById(product);
                }
            }
        }
    }

    @Override
    public OmsOrderReturnApplyResult getItem(Long id) {
        return returnApplyDao.getDetail(id);
    }
}
