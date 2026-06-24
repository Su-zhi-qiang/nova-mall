package com.su.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.exception.Asserts;
import com.su.mall.mapper.*;
import com.su.mall.model.*;
import com.su.mall.portal.dao.SmsCouponHistoryDao;
import com.su.mall.common.service.RedisService;
import com.su.mall.portal.component.CouponClaimSender;
import com.su.mall.portal.domain.CouponClaimMessage;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.SmsCouponHistoryDetail;
import com.su.mall.portal.service.UmsMemberCouponService;
import com.su.mall.portal.service.UmsMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.su.mall.portal.domain.coupon.CouponScopeStrategy;
import com.su.mall.portal.domain.coupon.CouponScopeStrategyFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private final CouponScopeStrategyFactory couponScopeStrategyFactory;
    private final RedisService redisService;
    private final CouponClaimSender couponClaimSender;
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
        if(coupon.getType() != null && coupon.getType() != 0 && coupon.getType() != 1){
            Asserts.fail("该优惠券不可领取");
        }
        if(coupon.getPlatform() != null && coupon.getPlatform() != 0 && coupon.getPlatform() != 1){
            Asserts.fail("该优惠券不支持当前平台");
        }
        if(coupon.getEnableTime() != null && now.before(coupon.getEnableTime())){
            Asserts.fail("优惠券还没到领取时间");
        }
        if(coupon.getStartTime() != null && now.before(coupon.getStartTime())){
            Asserts.fail("优惠券还没到使用时间");
        }
        if(coupon.getEndTime() != null && !now.before(coupon.getEndTime())){
            Asserts.fail("优惠券已过期");
        }
        if(coupon.getCount() == null || coupon.getCount() <= 0){
            Asserts.fail("优惠券已领完");
        }
        
        // 2. 判断用户已领取数量是否超过限制
        long receivedCount = couponHistoryMapper.selectCount(
                new LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getCouponId, couponId)
                        .eq(SmsCouponHistory::getMemberId, currentMember.getId()));
        if(receivedCount >= coupon.getPerLimit()){
            Asserts.fail("您已达到该优惠券的领取上限");
        }
        
        // 3. 确保Redis中有库存（首次访问时从DB同步）
        ensureCouponRedisStock(couponId, coupon.getCount());

        // 4. Redis预减库存（Lua原子操作，毫秒级拦截）
        Boolean success = redisService.deductCouponStock(couponId);
        if(success == null || !success){
            Asserts.fail("优惠券库存不足，请稍后再试");
        }

        // 4. 发送MQ消息，异步执行DB扣减 + 生成领取记录
        CouponClaimMessage message = new CouponClaimMessage();
        message.setCouponId(couponId);
        message.setMemberId(currentMember.getId());
        message.setMemberNickname(currentMember.getNickname());
        couponClaimSender.sendMessage(message);
    }

    /**
     * 确保Redis中有优惠券库存（首次访问时从DB同步）
     */
    private void ensureCouponRedisStock(Long couponId, Integer dbStock) {
        String redisKey = "coupon:stock:" + couponId;
        if (!Boolean.TRUE.equals(redisService.hasKey(redisKey))) {
            // 从DB读取当前剩余库存并同步到Redis
            SmsCoupon coupon = couponMapper.selectById(couponId);
            int stock = (coupon != null && coupon.getCount() != null) ? coupon.getCount() : 0;
            redisService.set(redisKey, String.valueOf(stock));
        }
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
        return couponHistoryMapper.selectList(new LambdaQueryWrapper<SmsCouponHistory>()
                .eq(SmsCouponHistory::getMemberId, currentMember.getId())
                .eq(useStatus != null, SmsCouponHistory::getUseStatus, useStatus)
                .orderByDesc(SmsCouponHistory::getCreateTime));
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
            // 检查优惠券是否已过期
            boolean isExpired = endTime != null && now.after(endTime);

            CouponScopeStrategy strategy = couponScopeStrategyFactory.getStrategy(useType);
            BigDecimal totalAmount = strategy.calcEligibleAmount(cartItemList, couponHistoryDetail);

            if (!isExpired && totalAmount.compareTo(BigDecimal.ZERO) > 0
                    && totalAmount.subtract(minPoint != null ? minPoint : BigDecimal.ZERO).compareTo(BigDecimal.ZERO) >= 0) {
                enableList.add(couponHistoryDetail);
            } else {
                disableList.add(couponHistoryDetail);
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
        return couponHistoryDao.getCouponList(member.getId(), useStatus);
    }

    @Override
    public List<SmsCoupon> listAvailable() {
        UmsMember currentMember = memberService.getCurrentMember();
        Date now = new Date();
        
        // 1. 一次查询所有符合条件的可领取优惠券
        LambdaQueryWrapper<SmsCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SmsCoupon::getType, 0, 1);  // 全场赠券 或 会员赠券
        wrapper.gt(SmsCoupon::getCount, 0);     // 仍有库存
        wrapper.and(w -> w.isNull(SmsCoupon::getEnableTime).or().le(SmsCoupon::getEnableTime, now));
        wrapper.and(w -> w.isNull(SmsCoupon::getStartTime).or().le(SmsCoupon::getStartTime, now));
        wrapper.and(w -> w.isNull(SmsCoupon::getEndTime).or().gt(SmsCoupon::getEndTime, now));
        wrapper.in(SmsCoupon::getPlatform, 0, 1);
        
        List<SmsCoupon> filteredCoupons = couponMapper.selectList(wrapper);
        if (CollUtil.isEmpty(filteredCoupons)) {
            return new ArrayList<>();
        }
        
        // 2. 批量查询用户对这些优惠券的已领取数量（解决N+1问题）
        List<Long> couponIds = filteredCoupons.stream().map(SmsCoupon::getId).collect(Collectors.toList());
        List<SmsCouponHistory> userHistory = couponHistoryMapper.selectList(
                new LambdaQueryWrapper<SmsCouponHistory>()
                        .in(SmsCouponHistory::getCouponId, couponIds)
                        .eq(SmsCouponHistory::getMemberId, currentMember.getId()));
        
        // 统计每张优惠券的已领取数量
        Map<Long, Long> receivedCountMap = userHistory.stream()
                .collect(Collectors.groupingBy(SmsCouponHistory::getCouponId, Collectors.counting()));
        
        // 3. 过滤掉用户已达到领取上限的优惠券
        List<SmsCoupon> availableCoupons = new ArrayList<>();
        for (SmsCoupon coupon : filteredCoupons) {
            long receivedCount = receivedCountMap.getOrDefault(coupon.getId(), 0L);
            if (receivedCount < coupon.getPerLimit()) {
                availableCoupons.add(coupon);
            }
        }
        
        return availableCoupons;
    }

    private BigDecimal calcTotalAmount(List<CartPromotionItem> cartItemList) {
        return calcTotalAmountByFilter(cartItemList, null, null);
    }

    private BigDecimal calcTotalAmountByproductCategoryId(List<CartPromotionItem> cartItemList, List<Long> productCategoryIds) {
        return calcTotalAmountByFilter(cartItemList, productCategoryIds, null);
    }

    private BigDecimal calcTotalAmountByProductId(List<CartPromotionItem> cartItemList, List<Long> productIds) {
        return calcTotalAmountByFilter(cartItemList, null, productIds);
    }

    private BigDecimal calcTotalAmountByFilter(List<CartPromotionItem> cartItemList, List<Long> productCategoryIds, List<Long> productIds) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartPromotionItem item : cartItemList) {
            if (productCategoryIds != null && !productCategoryIds.contains(item.getProductCategoryId())) {
                continue;
            }
            if (productIds != null && !productIds.contains(item.getProductId())) {
                continue;
            }
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
            total = total.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return total;
    }

}