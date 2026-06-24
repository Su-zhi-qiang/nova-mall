package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.service.RedisService;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.dao.PortalOrderDao;
import com.su.mall.mapper.SmsFlashPromotionDailyStockMapper;
import com.su.mall.model.OmsOrderItem;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 取消订单时恢复库存（DB + Redis）
 */
@RequiredArgsConstructor
public class RestoreStockForCancelHandler extends OrderHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestoreStockForCancelHandler.class);

    private final PortalOrderDao portalOrderDao;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;
    private final RedisService redisService;

    @Override
    public void handle(OrderHandlerContext context) {
        List<OmsOrderItem> orderItemList = context.getAttribute("cancelOrderItemList");
        Integer originalStatus = context.getAttribute("originalStatus");

        if (!CollectionUtils.isEmpty(orderItemList)) {
            if (originalStatus != null && originalStatus == 1) {
                // 已支付的订单：恢复真实库存 + 商品表库存
                portalOrderDao.restoreSkuStock(orderItemList);
                portalOrderDao.restoreProductStock(orderItemList);
            } else {
                // 未支付的订单：只释放锁定库存
                portalOrderDao.releaseSkuStockLock(orderItemList);
            }
            // 恢复秒杀商品库存和已售数量（DB）
            for (OmsOrderItem orderItem : orderItemList) {
                if (orderItem.getFlashPromotionRelationId() != null) {
                    dailyStockMapper.restoreStock(
                            orderItem.getFlashPromotionRelationId(),
                            orderItem.getProductQuantity()
                    );
                    // 恢复Redis预减库存
                    redisService.restoreSeckillStock(
                            orderItem.getFlashPromotionRelationId(),
                            orderItem.getProductQuantity());
                    LOGGER.info("取消秒杀订单恢复Redis库存, relationId: {}, quantity: {}",
                            orderItem.getFlashPromotionRelationId(), orderItem.getProductQuantity());
                }
            }
        }
        handleNext(context);
    }
}
