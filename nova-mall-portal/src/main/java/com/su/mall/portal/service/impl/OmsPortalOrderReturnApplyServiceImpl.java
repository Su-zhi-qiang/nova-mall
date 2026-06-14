package com.su.mall.portal.service.impl;

import com.su.mall.mapper.OmsOrderItemMapper;
import com.su.mall.mapper.OmsOrderMapper;
import com.su.mall.mapper.OmsOrderReturnApplyMapper;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.OmsOrderReturnApply;
import com.su.mall.portal.domain.OmsOrderReturnApplyParam;
import com.su.mall.portal.service.UmsMemberService;
import com.su.mall.portal.service.OmsPortalOrderReturnApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 订单退货管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class OmsPortalOrderReturnApplyServiceImpl implements OmsPortalOrderReturnApplyService {
    private final OmsOrderReturnApplyMapper returnApplyMapper;
    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final UmsMemberService memberService;

    @Override
    public int create(OmsOrderReturnApplyParam returnApply) {
        OmsOrderReturnApply realApply = new OmsOrderReturnApply();
        BeanUtils.copyProperties(returnApply, realApply);
        
        // 根据 orderId 自动填充订单和商品信息
        if (returnApply.getOrderId() != null) {
            OmsOrder order = orderMapper.selectById(returnApply.getOrderId());
            if (order != null) {
                realApply.setOrderSn(order.getOrderSn());
                realApply.setMemberUsername(order.getMemberUsername());
                
                // 获取订单中的第一个商品信息
                List<OmsOrderItem> orderItems = orderItemMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OmsOrderItem>()
                        .eq(OmsOrderItem::getOrderId, returnApply.getOrderId()));
                
                if (!CollectionUtils.isEmpty(orderItems)) {
                    OmsOrderItem item = orderItems.get(0);
                    realApply.setProductId(item.getProductId());
                    realApply.setProductName(item.getProductName());
                    realApply.setProductPic(item.getProductPic());
                    realApply.setProductAttr(item.getProductAttr());
                    realApply.setProductBrand(item.getProductBrand());
                    realApply.setProductPrice(item.getProductPrice());
                    realApply.setProductRealPrice(item.getRealAmount());
                    realApply.setProductCount(item.getProductQuantity());
                }
            }
        }
        
        realApply.setCreateTime(new Date());
        realApply.setStatus(0);
        return returnApplyMapper.insert(realApply);
    }
}
