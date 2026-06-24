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
 * 扣减库存（秒杀/普通）
 * 秒杀：已通过Redis预减库存拦截大部分请求，此处仅做DB原子扣减（兜底）
 * 普通：直接扣减SKU和商品库存
 */
@RequiredArgsConstructor
public class DeductStockHandler extends OrderHandler {

    private final PortalOrderDao portalOrderDao;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;
    private final SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;
    private final RedisService redisService;

    @Override
    public void handle(OrderHandlerContext context) {
        OmsOrder fullOrder = context.getAttribute("fullOrder");
        List<OmsOrderItem> orderItemList = context.getAttribute("orderItemList");

        if (fullOrder.getOrderType() != null && fullOrder.getOrderType() == 1) {
            // 秒杀订单：原子SQL扣减数据库库存（兜底，Redis已预减）
            for (OmsOrderItem orderItem : orderItemList) {
                if (orderItem.getFlashPromotionRelationId() != null) {
                    int result = dailyStockMapper.decreaseStock(
                            orderItem.getFlashPromotionRelationId(),
                            orderItem.getProductQuantity()
                    );
                    if (result == 0) {
                        // DB库存不足，恢复Redis库存
                        redisService.restoreSeckillStock(
                                orderItem.getFlashPromotionRelationId(),
                                orderItem.getProductQuantity());
                        Asserts.fail("秒杀库存不足");
                    }
                }
            }
            portalOrderDao.updateSkuStock(orderItemList);
            portalOrderDao.updateProductStock(orderItemList);
            clearFlashPromotionCache(orderItemList);
        } else {
            // 普通订单
            portalOrderDao.updateSkuStock(orderItemList);
            portalOrderDao.updateProductStock(orderItemList);
        }
        handleNext(context);
    }

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
