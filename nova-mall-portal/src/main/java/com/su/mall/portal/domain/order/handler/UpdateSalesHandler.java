package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.service.RedisService;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.dao.PortalOrderDao;
import com.su.mall.model.OmsOrderItem;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 支付成功链 - 第5步：更新销量并清除缓存
 * <p>增加商品和SKU的销量统计 → 清除商品详情缓存（确保销量数据刷新）
 *
 * @see OrderHandler
 */
@RequiredArgsConstructor
public class UpdateSalesHandler extends OrderHandler {

    private final PortalOrderDao portalOrderDao;
    private final RedisService redisService;

    @Override
    public void handle(OrderHandlerContext context) {
        // 1. 获取订单商品列表
        List<OmsOrderItem> orderItemList = context.getAttribute("orderItemList");

        // 2. 增加商品表和SKU表的销量
        portalOrderDao.updateProductSale(orderItemList);
        portalOrderDao.updateSkuSale(orderItemList);

        // 3. 清除商品详情缓存（下次访问会重新加载最新销量）
        for (OmsOrderItem item : orderItemList) {
            String cacheKey = "product:detail:" + item.getProductId();
            redisService.del(cacheKey);
        }

        // 4. 传递给下一个处理器
        handleNext(context);
    }
}
