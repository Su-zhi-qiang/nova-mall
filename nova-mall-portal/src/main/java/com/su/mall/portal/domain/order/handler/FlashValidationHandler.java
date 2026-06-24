package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.dao.PortalOrderDao;
import com.su.mall.mapper.SmsFlashPromotionDailyStockMapper;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FlashValidationHandler extends OrderHandler {

    private final SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;
    private final PortalOrderDao portalOrderDao;

    @Override
    public void handle(OrderHandlerContext context) {
        if (!context.isHasFlashPromotion()) {
            handleNext(context);
            return;
        }

        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            if (cartPromotionItem.getFlashPromotion() != null && cartPromotionItem.getFlashPromotion()) {
                if (cartPromotionItem.getFlashPromotionRelationId() == null) {
                    Asserts.fail("秒杀商品信息异常");
                }
                SmsFlashPromotionProductRelation relation = flashPromotionProductRelationMapper.selectById(
                        cartPromotionItem.getFlashPromotionRelationId());
                if (relation == null) {
                    Asserts.fail("秒杀活动不存在");
                }
                Integer dailyStock = dailyStockMapper.getCurrentStock(cartPromotionItem.getFlashPromotionRelationId());
                if (dailyStock == null || dailyStock < cartPromotionItem.getQuantity()) {
                    Asserts.fail("秒杀库存不足");
                }
                if (relation.getFlashPromotionLimit() != null && cartPromotionItem.getQuantity() > relation.getFlashPromotionLimit()) {
                    Asserts.fail("超出限购数量，每人限购" + relation.getFlashPromotionLimit() + "件");
                }
                int buyCount = portalOrderDao.getMemberFlashBuyCount(
                        context.getCurrentMember().getId(), cartPromotionItem.getFlashPromotionRelationId());
                if (relation.getFlashPromotionLimit() != null && buyCount + cartPromotionItem.getQuantity() > relation.getFlashPromotionLimit()) {
                    Asserts.fail("超出限购数量，您已购买" + buyCount + "件，每人限购" + relation.getFlashPromotionLimit() + "件");
                }
            }
        }
        handleNext(context);
    }
}