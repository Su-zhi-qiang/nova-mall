package com.su.mall.portal.domain.promotion;

import com.su.mall.model.PmsProductFullReduction;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 满减优惠策略实现
 * <p>负责计算商品参与满减活动时的优惠金额，采用按价格比例分摊的方式
 * <p>满减规则示例：满100元减20元、满200元减50元
 */
@Component
public class FullReductionPromotionStrategy implements PromotionStrategy {

    /**
     * 计算单个商品的满减优惠金额
     * <p>核心算法：按商品价格占订单总额的比例分摊满减优惠
     * <p>公式：reduceAmount = (商品原价 / 订单总额) × 满减金额
     *
     * @param context 促销计算上下文，包含商品信息、满减规则、订单总金额等
     * @return 包含优惠信息的购物车促销商品，无满减规则时返回null
     */
    @Override
    public CartPromotionItem calculatePromotion(PromotionContext context) {
        // 1. 获取满减优惠规则，无规则则不参与满减
        PmsProductFullReduction fullReduction = context.getFullReduction();
        if (fullReduction == null) {
            return null;
        }

        // 2. 创建促销商品对象，复制商品基础信息
        CartPromotionItem cartPromotionItem = new CartPromotionItem();
        BeanUtils.copyProperties(context.getItem(), cartPromotionItem);

        // 3. 设置促销活动描述信息（如："满100元，减20元"）
        String promotionMessage = getFullReductionPromotionMessage(fullReduction);
        cartPromotionItem.setPromotionMessage(promotionMessage);

        // 4. 获取商品SKU库存信息（包含原价、库存等）
        PmsSkuStock skuStock = getOriginalPrice(context.getPromotionProduct(), context.getItem().getProductSkuId());
        if (skuStock == null) {
            return null; // 未找到对应SKU，无法计算
        }

        // 5. 计算满减优惠金额（按比例分摊）
        BigDecimal originalPrice = skuStock.getPrice();          // 当前商品原价
        BigDecimal totalAmount = context.getTotalAmount();       // 订单中参与满减的商品总金额
        BigDecimal reduceAmount = originalPrice
                .divide(totalAmount, RoundingMode.HALF_EVEN)    // 计算比例，使用银行家舍入法
                .multiply(fullReduction.getReducePrice());      // 乘以满减金额

        // 6. 设置优惠金额到促销商品
        cartPromotionItem.setReduceAmount(reduceAmount);

        // 7. 设置实际可用库存（总库存 - 锁定库存）
        Integer lockStockVal = skuStock.getLockStock();
        int lockStock = lockStockVal == null ? 0 : lockStockVal;
        cartPromotionItem.setRealStock(skuStock.getStock() - lockStock);

        // 8. 设置赠品积分和成长值
        cartPromotionItem.setIntegration(context.getPromotionProduct().getGiftPoint());
        cartPromotionItem.setGrowth(context.getPromotionProduct().getGiftGrowth());

        return cartPromotionItem;
    }

    /**
     * 构建满减优惠描述信息
     *
     * @param fullReduction 满减规则配置
     * @return 格式化的促销描述，如："满减优惠：满100元，减20元"
     */
    private String getFullReductionPromotionMessage(PmsProductFullReduction fullReduction) {
        StringBuilder sb = new StringBuilder();
        sb.append("满减优惠：");
        sb.append("满");
        sb.append(fullReduction.getFullPrice());
        sb.append("元，");
        sb.append("减");
        sb.append(fullReduction.getReducePrice());
        sb.append("元");
        return sb.toString();
    }

    /**
     * 根据SKU ID查找对应的库存信息
     *
     * @param promotionProduct 促销商品信息（包含SKU列表）
     * @param productSkuId     商品SKU ID
     * @return 对应的SKU库存信息，未找到返回null
     */
    private PmsSkuStock getOriginalPrice(PromotionProduct promotionProduct, Long productSkuId) {
        for (PmsSkuStock skuStock : promotionProduct.getSkuStockList()) {
            if (productSkuId.equals(skuStock.getId())) {
                return skuStock;
            }
        }
        return null;
    }
}