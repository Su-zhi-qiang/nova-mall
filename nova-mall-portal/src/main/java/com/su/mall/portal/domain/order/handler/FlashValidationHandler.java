package com.su.mall.portal.domain.order.handler;

import com.su.mall.common.exception.Asserts;
import com.su.mall.common.service.RedisService;
import com.su.mall.portal.domain.order.OrderHandler;
import com.su.mall.portal.domain.order.OrderHandlerContext;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.dao.PortalOrderDao;
import com.su.mall.mapper.SmsFlashPromotionDailyStockMapper;
import com.su.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 秒杀校验：Redis预减库存 + 活动/限购校验
 * Redis预减成功后，库存由DB原子SQL兜底扣减
 */
@RequiredArgsConstructor
public class FlashValidationHandler extends OrderHandler {

    private final SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;
    private final SmsFlashPromotionDailyStockMapper dailyStockMapper;
    private final PortalOrderDao portalOrderDao;
    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void handle(OrderHandlerContext context) {
        if (!context.isHasFlashPromotion()) {
            handleNext(context);
            return;
        }

        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();
        List<Long> preDeductedRelationIds = new ArrayList<>();
        List<Integer> preDeductedQuantities = new ArrayList<>();

        try {
            for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
                if (cartPromotionItem.getFlashPromotion() != null && cartPromotionItem.getFlashPromotion()) {
                    Long relationId = cartPromotionItem.getFlashPromotionRelationId();
                    if (relationId == null) {
                        Asserts.fail("秒杀商品信息异常");
                    }

                    SmsFlashPromotionProductRelation relation = flashPromotionProductRelationMapper.selectById(relationId);
                    if (relation == null) {
                        Asserts.fail("秒杀活动不存在");
                    }

                    // 限购校验
                    if (relation.getFlashPromotionLimit() != null && cartPromotionItem.getQuantity() > relation.getFlashPromotionLimit()) {
                        Asserts.fail("超出限购数量，每人限购" + relation.getFlashPromotionLimit() + "件");
                    }
                    int buyCount = portalOrderDao.getMemberFlashBuyCount(
                            context.getCurrentMember().getId(), relationId);
                    if (relation.getFlashPromotionLimit() != null && buyCount + cartPromotionItem.getQuantity() > relation.getFlashPromotionLimit()) {
                        Asserts.fail("超出限购数量，您已购买" + buyCount + "件，每人限购" + relation.getFlashPromotionLimit() + "件");
                    }

                    // 确保Redis中有库存（首次访问时从DB同步）
                    ensureSeckillRedisStock(relationId);

                    // Redis预减库存（Lua原子操作，毫秒级拦截）
                    Boolean success = redisService.deductSeckillStock(relationId);
                    if (success == null) {
                        Asserts.fail("秒杀库存不足");
                    }
                    if (!success) {
                        Asserts.fail("秒杀库存已售罄");
                    }
                    preDeductedRelationIds.add(relationId);
                    preDeductedQuantities.add(cartPromotionItem.getQuantity());
                }
            }
            // 记录预扣信息，用于取消订单时恢复Redis库存
            context.putAttribute("preDeductedRelationIds", preDeductedRelationIds);
            context.putAttribute("preDeductedQuantities", preDeductedQuantities);
            handleNext(context);
        } catch (Exception e) {
            // 预减成功但后续步骤失败，恢复Redis库存
            for (int i = 0; i < preDeductedRelationIds.size(); i++) {
                redisService.restoreSeckillStock(preDeductedRelationIds.get(i), preDeductedQuantities.get(i));
            }
            throw e;
        }
    }

    /**
     * 确保Redis中有秒杀库存（首次访问时从DB同步）
     * 使用StringRedisSerializer存储，避免Jackson序列化导致Lua脚本无法解析
     */
    private void ensureSeckillRedisStock(Long relationId) {
        String redisKey = "seckill:stock:" + relationId;
        if (!Boolean.TRUE.equals(redisService.hasKey(redisKey))) {
            Integer stock = dailyStockMapper.getCurrentStock(relationId);
            String stockStr = stock != null ? stock.toString() : "0";
            StringRedisSerializer serializer = new StringRedisSerializer();
            redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                connection.set(serializer.serialize(redisKey), serializer.serialize(stockStr));
                return null;
            });
        }
    }
}
