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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 扣减库存（秒杀/普通）
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
            // 秒杀订单
            List<String> acquiredLocks = new ArrayList<>();
            String lockValue = UUID.randomUUID().toString();
            try {
                for (OmsOrderItem orderItem : orderItemList) {
                    if (orderItem.getFlashPromotionRelationId() != null) {
                        String lockKey = "flash:lock:relation:" + orderItem.getFlashPromotionRelationId();
                        Boolean locked = redisService.tryLock(lockKey, lockValue, 30);
                        if (!locked) {
                            Asserts.fail("当前抢购人数过多，请稍后重试");
                        }
                        acquiredLocks.add(lockKey);
                    }
                }
                for (OmsOrderItem orderItem : orderItemList) {
                    if (orderItem.getFlashPromotionRelationId() != null) {
                        int result = dailyStockMapper.decreaseStock(
                                orderItem.getFlashPromotionRelationId(),
                                orderItem.getProductQuantity()
                        );
                        if (result == 0) {
                            Asserts.fail("秒杀库存不足");
                        }
                    }
                }
                portalOrderDao.updateSkuStock(orderItemList);
                portalOrderDao.updateProductStock(orderItemList);
                clearFlashPromotionCache(orderItemList);
            } finally {
                for (String lockKey : acquiredLocks) {
                    redisService.releaseLock(lockKey, lockValue);
                }
            }
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