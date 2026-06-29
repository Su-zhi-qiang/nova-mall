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
 * 订单取消链 - 第3步：恢复库存
 * <p>根据订单原始状态区分处理：
 * <ul>
 *   <li>已支付(status=1)：恢复真实库存 + 商品表库存</li>
 *   <li>未支付(status=0)：仅释放锁定库存</li>
 * </ul>
 * <p>秒杀商品额外恢复：每日库存快照(DB) + Redis预减库存
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class RestoreStockForCancelHandler extends OrderHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestoreStockForCancelHandler.class);

    private final PortalOrderDao portalOrderDao;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;
    private final RedisService redisService;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取订单商品列表和原始订单状态
        List<OmsOrderItem> orderItemList = context.getAttribute("cancelOrderItemList");
        Integer originalStatus = context.getAttribute("originalStatus");

        if (!CollectionUtils.isEmpty(orderItemList)) {
            if (originalStatus != null && originalStatus == 1) {
                // 2. 已支付订单：恢复SKU真实库存和商品表库存
                portalOrderDao.restoreSkuStock(orderItemList);
                portalOrderDao.restoreProductStock(orderItemList);
            } else {
                // 3. 未支付订单：仅释放锁定库存
                portalOrderDao.releaseSkuStockLock(orderItemList);
            }

            // 4. 恢复秒杀商品库存（DB + Redis）
            for (OmsOrderItem orderItem : orderItemList) {
                if (orderItem.getFlashPromotionRelationId() != null) {
                    // 5. 恢复秒杀每日库存快照（DB）
                    dailyStockMapper.restoreStock(
                            orderItem.getFlashPromotionRelationId(),
                            orderItem.getProductQuantity()
                    );

                    // 6. 恢复Redis预减库存
                    redisService.restoreSeckillStock(
                            orderItem.getFlashPromotionRelationId(),
                            orderItem.getProductQuantity());
                    LOGGER.info("取消秒杀订单恢复Redis库存, relationId: {}, quantity: {}",
                            orderItem.getFlashPromotionRelationId(), orderItem.getProductQuantity());
                }
            }
        }

        // 7. 传递给下一个处理器
        handleNext(context);
    }
}
