package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.common.service.RedisService;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.dao.PortalOrderDao;
import com.su.mall.mapper.SmsFlashPromotionDailyStockMapper;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 支付成功链 - 第4步：扣减库存
 * <p>秒杀订单：先原子SQL扣减秒杀库存（DB兜底）→ 扣减SKU和商品库存 → 清除秒杀缓存
 * <p>普通订单：直接扣减SKU和商品库存
 * <p>DB扣减失败时恢复Redis库存（补偿机制）
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class DeductStockHandler extends OrderHandler {

    private final PortalOrderDao portalOrderDao;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;
    private final SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;
    private final RedisService redisService;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取订单信息和订单商品列表
        OmsOrder fullOrder = context.getAttribute("fullOrder");
        List<OmsOrderItem> orderItemList = context.getAttribute("orderItemList");

        // 2. 判断是否为秒杀订单（orderType=1）
        if (fullOrder.getOrderType() != null && fullOrder.getOrderType() == 1) {
            // 3. 秒杀订单：原子SQL扣减秒杀每日库存（Redis已预减，此处为DB兜底）
            for (OmsOrderItem orderItem : orderItemList) {
                if (orderItem.getFlashPromotionRelationId() != null) {
                    int result = dailyStockMapper.decreaseStock(
                            orderItem.getFlashPromotionRelationId(),
                            orderItem.getProductQuantity()
                    );
                    if (result == 0) {
                        // 4. DB库存不足，恢复Redis预减库存（补偿）
                        redisService.restoreSeckillStock(
                                orderItem.getFlashPromotionRelationId(),
                                orderItem.getProductQuantity());
                        Asserts.fail("秒杀库存不足");
                    }
                }
            }

            // 5. 扣减SKU库存和商品库存
            portalOrderDao.updateSkuStock(orderItemList);
            portalOrderDao.updateProductStock(orderItemList);

            // 6. 清除秒杀活动的首页缓存（确保库存数据刷新）
            clearFlashPromotionCache(orderItemList);
        } else {
            // 7. 普通订单：直接扣减SKU库存和商品库存
            portalOrderDao.updateSkuStock(orderItemList);
            portalOrderDao.updateProductStock(orderItemList);
        }

        // 8. 传递给下一个处理器
        handleNext(context);
    }

    /**
     * 清除秒杀活动的首页缓存
     */
    private void clearFlashPromotionCache(List<OmsOrderItem> orderItemList) {
        for (OmsOrderItem item : orderItemList) {
            if (item.getFlashPromotionRelationId() != null) {
                SmsFlashPromotionProductRelation relation = flashPromotionProductRelationMapper.selectById(item.getFlashPromotionRelationId());
                if (relation != null) {
                    String cacheKey = "home:flashPromotion:" + relation.getFlashPromotionId() + ":" + relation.getFlashPromotionSessionId();
                    redisService.del(cacheKey);
                }
            }
        }
    }
}
