package com.su.mall.portal.service.impl;

import com.su.mall.model.OmsCartItem;
import com.su.mall.model.PmsProductFullReduction;
import com.su.mall.model.PmsProductLadder;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.dao.PortalProductDao;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
import com.su.mall.portal.domain.promotion.PromotionContext;
import com.su.mall.portal.domain.promotion.PromotionStrategy;
import com.su.mall.portal.domain.promotion.PromotionStrategyFactory;
import com.su.mall.portal.service.OmsPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author Su
 * 促销管理Service实现类
 */
@Service
@RequiredArgsConstructor
public class OmsPromotionServiceImpl implements OmsPromotionService {
    private final PortalProductDao portalProductDao;
    private final com.su.mall.mapper.SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;
    private final com.su.mall.mapper.SmsFlashPromotionDailyStockMapper dailyStockMapper;
    private final PromotionStrategyFactory promotionStrategyFactory;

    @Override
    public List<CartPromotionItem> calcCartPromotion(List<OmsCartItem> cartItemList) {
        //1.先根据productId对CartItem进行分组，以spu为单位进行计算优惠
        Map<Long, List<OmsCartItem>> productCartMap = groupCartItemBySpu(cartItemList);
        //2.查询所有商品的优惠相关信息
        List<PromotionProduct> promotionProductList = getPromotionProductList(cartItemList);
        //3.根据商品促销类型计算商品促销优惠价格
        List<CartPromotionItem> cartPromotionItemList = new ArrayList<>();
        for (Map.Entry<Long, List<OmsCartItem>> entry : productCartMap.entrySet()) {
            Long productId = entry.getKey();
            PromotionProduct promotionProduct = getPromotionProductById(productId, promotionProductList);
            List<OmsCartItem> itemList = entry.getValue();
            if (promotionProduct == null) {
                // 商品无促销信息，按无优惠处理
                PromotionStrategy noStrategy = promotionStrategyFactory.getStrategy(0);
                for (OmsCartItem item : itemList) {
                    PromotionContext context = PromotionContext.builder()
                            .item(item)
                            .promotionProduct(promotionProduct)
                            .build();
                    CartPromotionItem cartPromotionItem = noStrategy.calculatePromotion(context);
                    if (cartPromotionItem != null) {
                        cartPromotionItemList.add(cartPromotionItem);
                    }
                }
                continue;
            }
            Integer promotionType = promotionProduct.getPromotionType();

            // 拆分秒杀项和普通项：秒杀商品优先使用购物车中已保存的 flashPromotionRelationId 直接查询
            List<OmsCartItem> flashItemList = new ArrayList<>();
            List<OmsCartItem> normalItemList = new ArrayList<>();
            // 缓存已查询的秒杀关联，避免重复查询
            Map<Long, com.su.mall.model.SmsFlashPromotionProductRelation> flashRelationMap = new HashMap<>();
            for (OmsCartItem item : itemList) {
                if (item.getFlashPromotionRelationId() != null) {
                    com.su.mall.model.SmsFlashPromotionProductRelation flashRelation =
                            flashPromotionProductRelationMapper.selectById(item.getFlashPromotionRelationId());
                    if (flashRelation != null && flashRelation.getFlashPromotionPrice() != null) {
                        Integer dailyStock = dailyStockMapper.getCurrentStock(item.getFlashPromotionRelationId());
                        if (dailyStock != null && dailyStock > 0) {
                            flashItemList.add(item);
                            flashRelationMap.put(item.getFlashPromotionRelationId(), flashRelation);
                            continue;
                        }
                    }
                }
                normalItemList.add(item);
            }

            // 秒杀项按秒杀策略处理
            PromotionStrategy flashStrategy = promotionStrategyFactory.getFlashStrategy();
            for (OmsCartItem item : flashItemList) {
                if (flashStrategy == null) {
                    // 秒杀策略未注册，按无优惠处理
                    PromotionStrategy noStrategy = promotionStrategyFactory.getStrategy(0);
                    com.su.mall.model.SmsFlashPromotionProductRelation flashRelation =
                            flashRelationMap.get(item.getFlashPromotionRelationId());
                    PromotionContext context = PromotionContext.builder()
                            .item(item)
                            .promotionProduct(promotionProduct)
                            .flashRelation(flashRelation)
                            .dailyStock(dailyStockMapper.getCurrentStock(flashRelation.getId()))
                            .build();
                    CartPromotionItem cartPromotionItem = noStrategy.calculatePromotion(context);
                    if (cartPromotionItem != null) {
                        cartPromotionItemList.add(cartPromotionItem);
                    }
                    continue;
                }
                com.su.mall.model.SmsFlashPromotionProductRelation flashRelation =
                        flashRelationMap.get(item.getFlashPromotionRelationId());
                if (flashRelation == null) continue;
                PromotionContext context = PromotionContext.builder()
                        .item(item)
                        .promotionProduct(promotionProduct)
                        .flashRelation(flashRelation)
                        .dailyStock(dailyStockMapper.getCurrentStock(flashRelation.getId()))
                        .build();
                CartPromotionItem cartPromotionItem = flashStrategy.calculatePromotion(context);
                if (cartPromotionItem != null) {
                    cartPromotionItemList.add(cartPromotionItem);
                }
            }

            // 普通项走原来的促销逻辑
            itemList = normalItemList;
            if (!itemList.isEmpty()) {
                PromotionStrategy strategy = promotionStrategyFactory.getStrategy(promotionType);
                // 对于打折和满减，需要先计算ladder或fullReduction
                if (promotionType != null && promotionType == 3) {
                    int count = getCartItemCount(itemList);
                    PmsProductLadder ladder = getProductLadder(count, promotionProduct.getProductLadderList());
                    if (ladder != null) {
                        for (OmsCartItem item : itemList) {
                            PromotionContext context = PromotionContext.builder()
                                    .item(item)
                                    .promotionProduct(promotionProduct)
                                    .ladder(ladder)
                                    .build();
                            CartPromotionItem cartPromotionItem = strategy.calculatePromotion(context);
                            if (cartPromotionItem != null) {
                                cartPromotionItemList.add(cartPromotionItem);
                            }
                        }
                    } else {
                        // 没有满足条件的打折策略，按无优惠处理
                        PromotionStrategy noStrategy = promotionStrategyFactory.getStrategy(0);
                        for (OmsCartItem item : itemList) {
                            PromotionContext context = PromotionContext.builder()
                                    .item(item)
                                    .promotionProduct(promotionProduct)
                                    .build();
                            CartPromotionItem cartPromotionItem = noStrategy.calculatePromotion(context);
                            if (cartPromotionItem != null) {
                                cartPromotionItemList.add(cartPromotionItem);
                            }
                        }
                    }
                } else if (promotionType != null && promotionType == 4) {
                    BigDecimal totalAmount = getCartItemAmount(itemList, promotionProductList);
                    PmsProductFullReduction fullReduction = getProductFullReduction(totalAmount, promotionProduct.getProductFullReductionList());
                    if (fullReduction != null) {
                        for (OmsCartItem item : itemList) {
                            PromotionContext context = PromotionContext.builder()
                                    .item(item)
                                    .promotionProduct(promotionProduct)
                                    .fullReduction(fullReduction)
                                    .totalAmount(totalAmount)
                                    .build();
                            CartPromotionItem cartPromotionItem = strategy.calculatePromotion(context);
                            if (cartPromotionItem != null) {
                                cartPromotionItemList.add(cartPromotionItem);
                            }
                        }
                    } else {
                        // 没有满足条件的满减策略，按无优惠处理
                        PromotionStrategy noStrategy = promotionStrategyFactory.getStrategy(0);
                        for (OmsCartItem item : itemList) {
                            PromotionContext context = PromotionContext.builder()
                                    .item(item)
                                    .promotionProduct(promotionProduct)
                                    .build();
                            CartPromotionItem cartPromotionItem = noStrategy.calculatePromotion(context);
                            if (cartPromotionItem != null) {
                                cartPromotionItemList.add(cartPromotionItem);
                            }
                        }
                    }
                } else {
                    // 其他促销类型（包括单品促销和无优惠）
                    for (OmsCartItem item : itemList) {
                        PromotionContext context = PromotionContext.builder()
                                .item(item)
                                .promotionProduct(promotionProduct)
                                .build();
                        CartPromotionItem cartPromotionItem = strategy.calculatePromotion(context);
                        if (cartPromotionItem != null) {
                            cartPromotionItemList.add(cartPromotionItem);
                        }
                    }
                }
            }
        }
        return cartPromotionItemList;
    }

    /**
     * 查询所有商品的优惠相关信息
     */
    private List<PromotionProduct> getPromotionProductList(List<OmsCartItem> cartItemList) {
        List<Long> productIdList = new ArrayList<>();
        for(OmsCartItem cartItem:cartItemList){
            productIdList.add(cartItem.getProductId());
        }
        return portalProductDao.getPromotionProductList(productIdList);
    }

    /**
     * 以spu为单位对购物车中商品进行分组
     */
    private Map<Long, List<OmsCartItem>> groupCartItemBySpu(List<OmsCartItem> cartItemList) {
        Map<Long, List<OmsCartItem>> productCartMap = new TreeMap<>();
        for (OmsCartItem cartItem : cartItemList) {
            List<OmsCartItem> productCartItemList = productCartMap.get(cartItem.getProductId());
            if (productCartItemList == null) {
                productCartItemList = new ArrayList<>();
                productCartItemList.add(cartItem);
                productCartMap.put(cartItem.getProductId(), productCartItemList);
            } else {
                productCartItemList.add(cartItem);
            }
        }
        return productCartMap;
    }



    private PmsProductFullReduction getProductFullReduction(BigDecimal totalAmount,List<PmsProductFullReduction> fullReductionList) {
        //按条件从高到低排序
        fullReductionList.sort(new Comparator<PmsProductFullReduction>() {
            @Override
            public int compare(PmsProductFullReduction o1, PmsProductFullReduction o2) {
                return o2.getFullPrice().subtract(o1.getFullPrice()).intValue();
            }
        });
        for(PmsProductFullReduction fullReduction:fullReductionList){
            if(totalAmount.subtract(fullReduction.getFullPrice()).intValue()>=0){
                return fullReduction;
            }
        }
        return null;
    }

    /**
     * 根据购买商品数量获取满足条件的打折优惠策略
     */
    private PmsProductLadder getProductLadder(int count, List<PmsProductLadder> productLadderList) {
        //按数量从大到小排序
        productLadderList.sort(new Comparator<PmsProductLadder>() {
            @Override
            public int compare(PmsProductLadder o1, PmsProductLadder o2) {
                return o2.getCount() - o1.getCount();
            }
        });
        for (PmsProductLadder productLadder : productLadderList) {
            if (count >= productLadder.getCount()) {
                return productLadder;
            }
        }
        return null;
    }

    /**
     * 获取购物车中指定商品的数量
     */
    private int getCartItemCount(List<OmsCartItem> itemList) {
        int count = 0;
        for (OmsCartItem item : itemList) {
            count += item.getQuantity();
        }
        return count;
    }

    /**
     * 获取购物车中指定商品的总价
     */
    private BigDecimal getCartItemAmount(List<OmsCartItem> itemList, List<PromotionProduct> promotionProductList) {
        BigDecimal amount = new BigDecimal(0);
        for (OmsCartItem item : itemList) {
            //计算出商品原价
            PromotionProduct promotionProduct = getPromotionProductById(item.getProductId(), promotionProductList);
            if (promotionProduct == null) continue;
            PmsSkuStock skuStock = getOriginalPrice(promotionProduct,item.getProductSkuId());
            if (skuStock == null) continue;
            amount = amount.add(skuStock.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        return amount;
    }

    /**
     * 获取商品的原价
     */
    private PmsSkuStock getOriginalPrice(PromotionProduct promotionProduct, Long productSkuId) {
        for (PmsSkuStock skuStock : promotionProduct.getSkuStockList()) {
            if (productSkuId.equals(skuStock.getId())) {
                return skuStock;
            }
        }
        return null;
    }

    /**
     * 根据商品id获取商品的促销信息
     */
    private PromotionProduct getPromotionProductById(Long productId, List<PromotionProduct> promotionProductList) {
        for (PromotionProduct promotionProduct : promotionProductList) {
            if (productId.equals(promotionProduct.getId())) {
                return promotionProduct;
            }
        }
        return null;
    }
}