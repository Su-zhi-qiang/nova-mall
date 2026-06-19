package com.su.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.su.mall.common.exception.Asserts;
import com.su.mall.mapper.*;
import com.su.mall.model.*;
import com.su.mall.portal.dao.SmsCouponHistoryDao;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.SmsCouponHistoryDetail;
import com.su.mall.portal.service.UmsMemberCouponService;
import com.su.mall.portal.service.UmsMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Objects;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 会员优惠券管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class UmsMemberCouponServiceImpl implements UmsMemberCouponService {
    private final UmsMemberService memberService;
    private final SmsCouponMapper couponMapper;
    private final SmsCouponHistoryMapper couponHistoryMapper;
    private final SmsCouponHistoryDao couponHistoryDao;
    private final SmsCouponProductRelationMapper couponProductRelationMapper;
    private final SmsCouponProductCategoryRelationMapper couponProductCategoryRelationMapper;
    private final PmsProductMapper productMapper;
    @Override
    @Transactional
    public void add(Long couponId) {
        UmsMember currentMember = memberService.getCurrentMember();
        Date now = new Date();
        
        // 1. 获取优惠券信息并校验基础条件
        SmsCoupon coupon = couponMapper.selectById(couponId);
        if(coupon==null){
            Asserts.fail("优惠券不存在");
        }
        if(now.before(coupon.getEnableTime())){
            Asserts.fail("优惠券还没到领取时间");
        }
        if(coupon.getEndTime() != null && !now.before(coupon.getEndTime())){
            Asserts.fail("优惠券已过期");
        }
        
        // 2. 判断用户已领取数量是否超过限制
        long receivedCount = couponHistoryMapper.selectCount(
                new LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getCouponId, couponId)
                        .eq(SmsCouponHistory::getMemberId, currentMember.getId()));
        if(receivedCount >= coupon.getPerLimit()){
            Asserts.fail("您已达到该优惠券的领取上限");
        }
        
        // 3. 使用乐观锁扣减库存（防止超领）
        // 条件：count > 0，确保有库存
        // 更新：count - 1, receiveCount + 1
        LambdaUpdateWrapper<SmsCoupon> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SmsCoupon::getId, couponId)
                    .gt(SmsCoupon::getCount, 0)  // 乐观锁条件：库存必须大于0
                    .set(SmsCoupon::getCount, coupon.getCount() - 1)
                    .set(SmsCoupon::getReceiveCount, 
                         coupon.getReceiveCount() == null ? 1 : coupon.getReceiveCount() + 1);
        
        int updateResult = couponMapper.update(null, updateWrapper);
        if(updateResult == 0){
            Asserts.fail("优惠券已领完，请稍后再试");
        }
        
        // 4. 生成领取记录
        SmsCouponHistory couponHistory = new SmsCouponHistory();
        couponHistory.setCouponId(couponId);
        couponHistory.setCouponCode(generateCouponCode(currentMember.getId()));
        couponHistory.setCreateTime(now);
        couponHistory.setMemberId(currentMember.getId());
        couponHistory.setMemberNickname(currentMember.getNickname());
        couponHistory.setGetType(1);  // 主动领取
        couponHistory.setUseStatus(0);  // 未使用
        couponHistoryMapper.insert(couponHistory);
    }

    /**
     * 16位优惠码生成：时间戳后8位+4位随机数+用户id后4位
     */
    private String generateCouponCode(Long memberId) {
        StringBuilder sb = new StringBuilder();
        Long currentTimeMillis = System.currentTimeMillis();
        String timeMillisStr = currentTimeMillis.toString();
        sb.append(timeMillisStr.substring(timeMillisStr.length() - 8));
        for (int i = 0; i < 4; i++) {
            sb.append(new Random().nextInt(10));
        }
        String memberIdStr = memberId.toString();
        if (memberIdStr.length() <= 4) {
            sb.append(String.format("%04d", memberId));
        } else {
            sb.append(memberIdStr.substring(memberIdStr.length()-4));
        }
        return sb.toString();
    }

    @Override
    public List<SmsCouponHistory> listHistory(Integer useStatus) {
        UmsMember currentMember = memberService.getCurrentMember();
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<SmsCouponHistory>())
        return couponHistoryMapper.selectList(new LambdaQueryWrapper<SmsCouponHistory>()
                .eq(SmsCouponHistory::getMemberId, currentMember.getId())
                .eq(useStatus != null, SmsCouponHistory::getUseStatus, useStatus));
    }

    @Override
    public List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type) {
        UmsMember currentMember = memberService.getCurrentMember();
        Date now = new Date();
        //获取该用户所有优惠券
        List<SmsCouponHistoryDetail> allList = couponHistoryDao.getDetailList(currentMember.getId());
        //根据优惠券使用类型来判断优惠券是否可用
        List<SmsCouponHistoryDetail> enableList = new ArrayList<>();
        List<SmsCouponHistoryDetail> disableList = new ArrayList<>();
        for (SmsCouponHistoryDetail couponHistoryDetail : allList) {
            // 跳过已被删除的优惠券
            if (couponHistoryDetail.getCoupon() == null) {
                continue;
            }
            Integer useType = couponHistoryDetail.getCoupon().getUseType();
            BigDecimal minPoint = couponHistoryDetail.getCoupon().getMinPoint();
            Date endTime = couponHistoryDetail.getCoupon().getEndTime();
            if (Objects.equals(0, useType)) {
                //0->全场通用
                //判断是否满足优惠起点
                //计算购物车商品的总价
                BigDecimal totalAmount = calcTotalAmount(cartItemList);
                if(now.before(endTime)&&totalAmount.subtract(minPoint).intValue()>=0){
                    enableList.add(couponHistoryDetail);
                }else{
                    disableList.add(couponHistoryDetail);
                }
            } else if (Objects.equals(1, useType)) {
                //1->指定分类
                //计算指定分类商品的总价
                List<Long> productCategoryIds = new ArrayList<>();
                for (SmsCouponProductCategoryRelation categoryRelation : couponHistoryDetail.getCategoryRelationList()) {
                    productCategoryIds.add(categoryRelation.getProductCategoryId());
                }
                BigDecimal totalAmount = calcTotalAmountByproductCategoryId(cartItemList,productCategoryIds);
                if(now.before(endTime)&&totalAmount.intValue()>0&&totalAmount.subtract(minPoint).intValue()>=0){
                    enableList.add(couponHistoryDetail);
                }else{
                    disableList.add(couponHistoryDetail);
                }
            } else if (Objects.equals(2, useType)) {
                //2->指定商品
                //计算指定商品的总价
                List<Long> productIds = new ArrayList<>();
                for (SmsCouponProductRelation productRelation : couponHistoryDetail.getProductRelationList()) {
                    productIds.add(productRelation.getProductId());
                }
                BigDecimal totalAmount = calcTotalAmountByProductId(cartItemList,productIds);
                if(now.before(endTime)&&totalAmount.intValue()>0&&totalAmount.subtract(minPoint).intValue()>=0){
                    enableList.add(couponHistoryDetail);
                }else{
                    disableList.add(couponHistoryDetail);
                }
            }
        }
        if(type.equals(1)){
            return enableList;
        }else{
            return disableList;
        }
    }

    @Override
    public List<SmsCoupon> listByProduct(Long productId) {
        Date now = new Date();
        List<Long> allCouponIds = new ArrayList<>();

        // 1. 获取指定商品(useType=2)相关优惠券
        List<SmsCouponProductRelation> cprList = couponProductRelationMapper.selectList(
                new LambdaQueryWrapper<SmsCouponProductRelation>()
                        .eq(SmsCouponProductRelation::getProductId, productId));
        if (CollUtil.isNotEmpty(cprList)) {
            allCouponIds.addAll(cprList.stream().map(SmsCouponProductRelation::getCouponId).toList());
        }

        // 2. 获取指定分类(useType=1)相关优惠券
        PmsProduct product = productMapper.selectById(productId);
        if (product != null && product.getProductCategoryId() != null) {
            List<SmsCouponProductCategoryRelation> cpcrList = couponProductCategoryRelationMapper.selectList(
                    new LambdaQueryWrapper<SmsCouponProductCategoryRelation>()
                            .eq(SmsCouponProductCategoryRelation::getProductCategoryId, product.getProductCategoryId()));
            if (CollUtil.isNotEmpty(cpcrList)) {
                allCouponIds.addAll(cpcrList.stream().map(SmsCouponProductCategoryRelation::getCouponId).toList());
            }
        }

        // 3. 构建查询条件，返回：
        //    - 全场通用(useType=0) 且 可领取 的优惠券
        //    - 指定分类/指定商品 且 ID 匹配 且 可领取 的优惠券
        // 可领取条件：type在[0,1]、剩余数量>0、enableTime<=now、startTime<=now、endTime>now、平台兼容
        LambdaQueryWrapper<SmsCoupon> wrapper = new LambdaQueryWrapper<>();
        // 仅返回用户可主动领取的优惠券：全场赠券(0) 或 会员赠券(1)
        wrapper.in(SmsCoupon::getType, 0, 1);
        // 仍有库存
        wrapper.gt(SmsCoupon::getCount, 0);
        // 已到领取时间
        wrapper.le(SmsCoupon::getEnableTime, now);
        // 已开始使用（startTime <= now，startTime 可为空表示无限制）
        wrapper.and(w -> w.isNull(SmsCoupon::getStartTime).or().le(SmsCoupon::getStartTime, now));
        // 未过期
        wrapper.gt(SmsCoupon::getEndTime, now);
        // 平台兼容：全平台(0) 或 移动端(1)
        wrapper.in(SmsCoupon::getPlatform, 0, 1);

        // 关联商品/分类的优惠券：useType != 0 时需要 ID 在匹配列表中
        // useType = 0 (全场通用)：直接返回
        // useType = 1 或 2：需要 ID 在 allCouponIds 里
        if (CollUtil.isNotEmpty(allCouponIds)) {
            wrapper.and(w -> w.eq(SmsCoupon::getUseType, 0).or().in(SmsCoupon::getId, allCouponIds));
        } else {
            // 没有商品/分类匹配的优惠券，只返回全场通用的
            wrapper.eq(SmsCoupon::getUseType, 0);
        }

        return couponMapper.selectList(wrapper);
    }

    @Override
    public List<SmsCoupon> list(Integer useStatus) {
        UmsMember member = memberService.getCurrentMember();
        return couponHistoryDao.getCouponList(member.getId(),useStatus);
    }

    private BigDecimal calcTotalAmount(List<CartPromotionItem> cartItemList) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
            total=total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }

    private BigDecimal calcTotalAmountByproductCategoryId(List<CartPromotionItem> cartItemList,List<Long> productCategoryIds) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            if(productCategoryIds.contains(item.getProductCategoryId())){
                BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
                total=total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return total;
    }

    private BigDecimal calcTotalAmountByProductId(List<CartPromotionItem> cartItemList,List<Long> productIds) {
        BigDecimal total = new BigDecimal("0");
        for (CartPromotionItem item : cartItemList) {
            if(productIds.contains(item.getProductId())){
                BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
                total=total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return total;
    }

}