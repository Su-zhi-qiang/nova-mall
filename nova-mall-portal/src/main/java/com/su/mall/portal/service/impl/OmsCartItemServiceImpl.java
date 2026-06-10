package com.su.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.OmsCartItemMapper;
import com.su.mall.model.OmsCartItem;
import com.su.mall.model.UmsMember;
import com.su.mall.portal.dao.PortalProductDao;
import com.su.mall.portal.domain.CartProduct;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.service.OmsCartItemService;
import com.su.mall.portal.service.OmsPromotionService;
import com.su.mall.portal.service.UmsMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class OmsCartItemServiceImpl implements OmsCartItemService {
    private final OmsCartItemMapper cartItemMapper;
    private final PortalProductDao productDao;
    private final OmsPromotionService promotionService;
    private final UmsMemberService memberService;

    @Override
    @Transactional
    public int add(OmsCartItem cartItem) {
        int count;
        UmsMember currentMember =memberService.getCurrentMember();
        cartItem.setMemberId(currentMember.getId());
        cartItem.setMemberNickname(currentMember.getNickname());
        cartItem.setDeleteStatus(0);
        OmsCartItem existCartItem = getCartItem(cartItem);
        if (existCartItem == null) {
            cartItem.setCreateDate(new Date());
            count = cartItemMapper.insert(cartItem);
        } else {
            cartItem.setModifyDate(new Date());
            existCartItem.setQuantity(existCartItem.getQuantity() + cartItem.getQuantity());
            // ✅ 改造：updateByPrimaryKey → updateById
            count = cartItemMapper.updateById(existCartItem);
        }
        return count;
    }

        /**
 * 根据会员id,商品id和规格获取购物车中商品
 *
 * @author Su
 */
private OmsCartItem getCartItem(OmsCartItem cartItem) {
    // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<OmsCartItem>())
    List<OmsCartItem> cartItemList = cartItemMapper.selectList(
            new LambdaQueryWrapper<OmsCartItem>()
                    .eq(OmsCartItem::getMemberId, cartItem.getMemberId())
                    .eq(OmsCartItem::getProductId, cartItem.getProductId())
                    .eq(OmsCartItem::getDeleteStatus, 0)
                    .eq(cartItem.getProductSkuId() != null, OmsCartItem::getProductSkuId, cartItem.getProductSkuId()));
    if (!CollectionUtils.isEmpty(cartItemList)) {
        return cartItemList.get(0);
    }
    return null;
}

    @Override
    public List<OmsCartItem> list(Long memberId) {
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<OmsCartItem>())
        return cartItemMapper.selectList(
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getDeleteStatus, 0)
                        .eq(OmsCartItem::getMemberId, memberId));
    }

    @Override
    public List<CartPromotionItem> listPromotion(Long memberId, List<Long> cartIds) {
        List<OmsCartItem> cartItemList = list(memberId);
        if(CollUtil.isNotEmpty(cartIds)){
            cartItemList = cartItemList.stream().filter(item->cartIds.contains(item.getId())).collect(Collectors.toList());
        }
        List<CartPromotionItem> cartPromotionItemList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(cartItemList)){
            cartPromotionItemList = promotionService.calcCartPromotion(cartItemList);
        }
        return cartPromotionItemList;
    }

    @Override
    public int updateQuantity(Long id, Long memberId, Integer quantity) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setQuantity(quantity);
        // ✅ 改造：updateByExampleSelective → update(new LambdaQueryWrapper<OmsCartItem>())
        return cartItemMapper.update(cartItem,
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getDeleteStatus, 0)
                        .eq(OmsCartItem::getId, id)
                        .eq(OmsCartItem::getMemberId, memberId));
    }

    @Override
    public int delete(Long memberId, List<Long> ids) {
        OmsCartItem record = new OmsCartItem();
        record.setDeleteStatus(1);
        // ✅ 改造：updateByExampleSelective → update(new LambdaQueryWrapper<OmsCartItem>())
        return cartItemMapper.update(record,
                new LambdaQueryWrapper<OmsCartItem>()
                        .in(OmsCartItem::getId, ids)
                        .eq(OmsCartItem::getMemberId, memberId));
    }

    @Override
    public CartProduct getCartProduct(Long productId) {
        return productDao.getCartProduct(productId);
    }

    @Override
    @Transactional
    public int updateAttr(OmsCartItem cartItem) {
        //删除原购物车信息
        OmsCartItem updateCart = new OmsCartItem();
        updateCart.setId(cartItem.getId());
        updateCart.setModifyDate(new Date());
        updateCart.setDeleteStatus(1);
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        cartItemMapper.updateById(updateCart);
        cartItem.setId(null);
        add(cartItem);
        return 1;
    }

    @Override
    public int clear(Long memberId) {
        OmsCartItem record = new OmsCartItem();
        record.setDeleteStatus(1);
        // ✅ 改造：updateByExampleSelective → update(new LambdaQueryWrapper<OmsCartItem>())
        return cartItemMapper.update(record,
                new LambdaQueryWrapper<OmsCartItem>()
                        .eq(OmsCartItem::getMemberId, memberId));
    }
}
