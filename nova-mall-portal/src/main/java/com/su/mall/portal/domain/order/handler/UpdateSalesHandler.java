package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.service.RedisService;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.dao.PortalOrderDao;
import com.su.mall.model.OmsOrderItem;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 更新销量并清除缓存
 */
@RequiredArgsConstructor
public class UpdateSalesHandler extends OrderHandler {

    private final PortalOrderDao portalOrderDao;
    private final RedisService redisService;

    @Override
    public void handle(OrderHandlerContext context) {
        List<OmsOrderItem> orderItemList = context.getAttribute("orderItemList");
        portalOrderDao.updateProductSale(orderItemList);
        portalOrderDao.updateSkuSale(orderItemList);
        for (OmsOrderItem item : orderItemList) {
            String cacheKey = "product:detail:" + item.getProductId();
            redisService.del(cacheKey);
        }
        handleNext(context);
    }
}