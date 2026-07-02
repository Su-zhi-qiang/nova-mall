package com.su.mall.portal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.OmsOrderItemMapper;
import com.su.mall.mapper.OmsOrderMapper;
import com.su.mall.mapper.OmsOrderReturnApplyMapper;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.OmsOrderReturnApply;
import com.su.mall.model.UmsMember;
import com.su.mall.portal.domain.OmsOrderReturnApplyParam;
import com.su.mall.portal.service.UmsMemberService;
import com.su.mall.portal.service.OmsPortalOrderReturnApplyService;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.exception.Asserts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单退货管理Service实现类
 * 
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
        // 检查该订单是否已有退货申请（status: 0待处理 1退货中）
        Long existingCount = returnApplyMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OmsOrderReturnApply>()
                .eq(OmsOrderReturnApply::getOrderId, returnApply.getOrderId())
                .in(OmsOrderReturnApply::getStatus, 0, 1));
        if (existingCount > 0) {
            Asserts.fail("该订单已有退货申请，请勿重复提交");
        }

        OmsOrderReturnApply realApply = new OmsOrderReturnApply();
        BeanUtils.copyProperties(returnApply, realApply);
        
        // 根据 orderId 自动填充订单和商品信息
        if (returnApply.getOrderId() != null) {
            OmsOrder order = orderMapper.selectById(returnApply.getOrderId());
            if (order != null) {
                realApply.setOrderSn(order.getOrderSn());
                realApply.setMemberUsername(order.getMemberUsername());
                // 从订单收货地址获取退货人信息
                realApply.setReturnName(order.getReceiverName());
                realApply.setReturnPhone(order.getReceiverPhone());
                
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

    @Override
    public CommonPage<OmsOrderReturnApply> list(Integer pageNum, Integer pageSize) {
        UmsMember member = memberService.getCurrentMember();
        Page<OmsOrderReturnApply> page = new Page<>(pageNum, pageSize);
        page = returnApplyMapper.selectPage(page,
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OmsOrderReturnApply>()
                .eq(OmsOrderReturnApply::getMemberUsername, member.getUsername())
                .eq(OmsOrderReturnApply::getDeleteStatus, 0)
                .orderByDesc(OmsOrderReturnApply::getCreateTime));
        return CommonPage.restPage(page);
    }

    @Override
    public Map<Long, Integer> getReturnStatusByOrderIds(Long memberId) {
        UmsMember member = memberService.getCurrentMember();
        List<OmsOrderReturnApply> applies = returnApplyMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OmsOrderReturnApply>()
                .eq(OmsOrderReturnApply::getMemberUsername, member.getUsername())
                .eq(OmsOrderReturnApply::getDeleteStatus, 0)
                .in(OmsOrderReturnApply::getStatus, 0, 1));
        if (CollectionUtils.isEmpty(applies)) {
            return Collections.emptyMap();
        }
        return applies.stream().collect(Collectors.toMap(
            OmsOrderReturnApply::getOrderId,
            OmsOrderReturnApply::getStatus,
            (existing, replacement) -> existing));
    }

    @Override
    public int delete(Long id) {
        UmsMember member = memberService.getCurrentMember();
        OmsOrderReturnApply apply = returnApplyMapper.selectById(id);
        if (apply == null) {
            Asserts.fail("退货申请不存在");
        }
        if (!member.getUsername().equals(apply.getMemberUsername())) {
            Asserts.fail("不能删除他人的退货申请");
        }
        if (apply.getStatus() != 2 && apply.getStatus() != 3) {
            Asserts.fail("只能删除已完成或已拒绝的退货申请");
        }
        apply.setDeleteStatus(1);
        return returnApplyMapper.updateById(apply);
    }
}
