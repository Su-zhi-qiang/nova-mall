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
 * 订单创建链 - 第3步：秒杀校验（Redis预减库存 + 活动/限购校验）
 * <p>流程：检测秒杀商品 → 校验限购 → 确保Redis有库存 → Lua原子预减 → 记录预扣信息
 * <p>Redis预减成功后，库存由DB原子SQL兜底扣减；预减失败则中断下单
 * <p>若后续步骤失败，通过预扣信息恢复Redis库存
 *
 * @see OrderHandler
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
        // 1. 非秒杀订单直接跳过，传递给下一个处理器
        if (!context.isHasFlashPromotion()) {
            handleNext(context);
            return;
        }

        List<CartPromotionItem> cartPromotionItemList = context.getCartPromotionItemList();
        // 2. 记录预扣的关联ID和数量，用于异常时恢复Redis库存
        List<Long> preDeductedRelationIds = new ArrayList<>();
        List<Integer> preDeductedQuantities = new ArrayList<>();

        try {
            // 3. 遍历购物车中的秒杀商品
            for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
                if (cartPromotionItem.getFlashPromotion() != null && cartPromotionItem.getFlashPromotion()) {
                    Long relationId = cartPromotionItem.getFlashPromotionRelationId();
                    if (relationId == null) {
                        Asserts.fail("秒杀商品信息异常");
                    }

                    // 4. 查询秒杀活动关联信息
                    SmsFlashPromotionProductRelation relation = flashPromotionProductRelationMapper.selectById(relationId);
                    if (relation == null) {
                        Asserts.fail("秒杀活动不存在");
                    }

                    // 5. 限购校验：单品购买数量不能超过限购上限
                    if (relation.getFlashPromotionLimit() != null && cartPromotionItem.getQuantity() > relation.getFlashPromotionLimit()) {
                        Asserts.fail("超出限购数量，每人限购" + relation.getFlashPromotionLimit() + "件");
                    }

                    // 6. 限购校验：累计购买数量不能超过限购上限
                    int buyCount = portalOrderDao.getMemberFlashBuyCount(
                            context.getCurrentMember().getId(), relationId);
                    if (relation.getFlashPromotionLimit() != null && buyCount + cartPromotionItem.getQuantity() > relation.getFlashPromotionLimit()) {
                        Asserts.fail("超出限购数量，您已购买" + buyCount + "件，每人限购" + relation.getFlashPromotionLimit() + "件");
                    }

                    // 7. 确保Redis中有秒杀库存（首次访问时从DB同步到Redis）
                    ensureSeckillRedisStock(relationId);

                    // 8. Redis Lua原子预减库存（毫秒级拦截超卖）
                    Boolean success = redisService.deductSeckillStock(relationId);
                    if (success == null) {
                        Asserts.fail("秒杀库存不足");
                    }
                    if (!success) {
                        Asserts.fail("秒杀库存已售罄");
                    }

                    // 9. 记录本次预扣信息，用于后续恢复
                    preDeductedRelationIds.add(relationId);
                    preDeductedQuantities.add(cartPromotionItem.getQuantity());
                }
            }

            // 10. 将预扣信息存入上下文，供取消订单时恢复Redis库存使用
            context.putAttribute("preDeductedRelationIds", preDeductedRelationIds);
            context.putAttribute("preDeductedQuantities", preDeductedQuantities);

            // 11. 校验通过，传递给下一个处理器
            handleNext(context);
        } catch (Exception e) {
            // 12. 后续步骤失败，恢复已预扣的Redis库存（补偿机制）
            for (int i = 0; i < preDeductedRelationIds.size(); i++) {
                redisService.restoreSeckillStock(preDeductedRelationIds.get(i), preDeductedQuantities.get(i));
            }
            throw e;
        }
    }

    /**
     * 确保Redis中有秒杀库存
     * <p>首次访问时从DB同步到Redis，使用StringRedisSerializer避免Jackson序列化导致Lua脚本无法解析
     *
     * @param relationId 秒杀关联ID
     */
    private void ensureSeckillRedisStock(Long relationId) {
        String redisKey = "seckill:stock:" + relationId;

        // 1. 检查Redis中是否已有该秒杀活动的库存
        if (!Boolean.TRUE.equals(redisService.hasKey(redisKey))) {
            // 2. 从DB获取当日秒杀库存
            Integer stock = dailyStockMapper.getCurrentStock(relationId);
            String stockStr = stock != null ? stock.toString() : "0";

            // 3. 使用String序列化写入Redis（确保Lua脚本能正确解析）
            StringRedisSerializer serializer = new StringRedisSerializer();
            redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                connection.set(serializer.serialize(redisKey), serializer.serialize(stockStr));
                return null;
            });
        }
    }
}
