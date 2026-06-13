package com.su.mall.portal.service.impl;

import com.su.mall.model.OmsCartItem;
import com.su.mall.model.PmsProductFullReduction;
import com.su.mall.model.PmsProductLadder;
import com.su.mall.model.PmsSkuStock;
import com.su.mall.portal.dao.PortalProductDao;
import com.su.mall.portal.domain.CartPromotionItem;
import com.su.mall.portal.domain.PromotionProduct;
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
            assert promotionProduct != null;
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
                    if (flashRelation != null && flashRelation.getFlashPromotionCount() != null && flashRelation.getFlashPromotionCount() > 0) {
                        flashItemList.add(item);
                        flashRelationMap.put(item.getFlashPromotionRelationId(), flashRelation);
                        continue;
                    }
                }
                normalItemList.add(item);
            }

            // 秒杀项按秒杀逻辑处理
            for (OmsCartItem item : flashItemList) {
                com.su.mall.model.SmsFlashPromotionProductRelation flashRelation =
                        flashRelationMap.get(item.getFlashPromotionRelationId());
                if (flashRelation == null) continue;
                CartPromotionItem cartPromotionItem = new CartPromotionItem();
                BeanUtils.copyProperties(item, cartPromotionItem);
                cartPromotionItem.setPromotionMessage("限时秒杀");
                cartPromotionItem.setFlashPromotion(true);
                cartPromotionItem.setFlashPromotionId(flashRelation.getFlashPromotionId());
                cartPromotionItem.setFlashPromotionSessionId(flashRelation.getFlashPromotionSessionId());
                cartPromotionItem.setFlashPromotionRelationId(flashRelation.getId());

                PmsSkuStock skuStock = getOriginalPrice(promotionProduct, item.getProductSkuId());
                assert skuStock != null;
                BigDecimal originalPrice = skuStock.getPrice();
                cartPromotionItem.setPrice(originalPrice);
                cartPromotionItem.setReduceAmount(originalPrice.subtract(flashRelation.getFlashPromotionPrice()));
                int realStock = flashRelation.getFlashPromotionCount();
                cartPromotionItem.setRealStock(realStock);
                cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
                cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
                cartPromotionItemList.add(cartPromotionItem);
            }

            // 普通项走原来的促销逻辑
            itemList = normalItemList;
            if (!itemList.isEmpty() && promotionType != null && promotionType == 1) {
                //单品促销
                for (OmsCartItem item : itemList) {
                    CartPromotionItem cartPromotionItem = new CartPromotionItem();
                    BeanUtils.copyProperties(item,cartPromotionItem);
                    cartPromotionItem.setPromotionMessage("单品促销");
                    //商品原价-促销价
                    PmsSkuStock skuStock = getOriginalPrice(promotionProduct, item.getProductSkuId());
                    assert skuStock != null;
                    BigDecimal originalPrice = skuStock.getPrice();
                    //单品促销使用原价
                    cartPromotionItem.setPrice(originalPrice);
                    cartPromotionItem.setReduceAmount(originalPrice.subtract(skuStock.getPromotionPrice()));
                    Integer lockStockVal = skuStock.getLockStock();
                    int lockStock = lockStockVal == null ? 0 : lockStockVal;
                    cartPromotionItem.setRealStock(skuStock.getStock() - lockStock);
                    cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
                    cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
                    cartPromotionItemList.add(cartPromotionItem);
                }
            } else if (promotionType != null && promotionType == 3) {
                //打折优惠
                int count = getCartItemCount(itemList);
                PmsProductLadder ladder = getProductLadder(count, promotionProduct.getProductLadderList());
                if(ladder!=null){
                    for (OmsCartItem item : itemList) {
                        CartPromotionItem cartPromotionItem = new CartPromotionItem();
                        BeanUtils.copyProperties(item,cartPromotionItem);
                        String message = getLadderPromotionMessage(ladder);
                        cartPromotionItem.setPromotionMessage(message);
                        //商品原价-折扣*商品原价
                        PmsSkuStock skuStock = getOriginalPrice(promotionProduct,item.getProductSkuId());
                        assert skuStock != null;
                        BigDecimal originalPrice = skuStock.getPrice();
                        BigDecimal reduceAmount = originalPrice.subtract(ladder.getDiscount().multiply(originalPrice));
                        cartPromotionItem.setReduceAmount(reduceAmount);
                        Integer lockStockVal2 = skuStock.getLockStock();
                        int lockStock2 = lockStockVal2 == null ? 0 : lockStockVal2;
                        cartPromotionItem.setRealStock(skuStock.getStock() - lockStock2);
                        cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
                        cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
                        cartPromotionItemList.add(cartPromotionItem);
                    }
                }else{
                    handleNoReduce(cartPromotionItemList,itemList,promotionProduct);
                }
            } else if (promotionType != null && promotionType == 4) {
                //满减
                BigDecimal totalAmount= getCartItemAmount(itemList,promotionProductList);
                PmsProductFullReduction fullReduction = getProductFullReduction(totalAmount,promotionProduct.getProductFullReductionList());
                if(fullReduction!=null){
                    for (OmsCartItem item : itemList) {
                        CartPromotionItem cartPromotionItem = new CartPromotionItem();
                        BeanUtils.copyProperties(item,cartPromotionItem);
                        String message = getFullReductionPromotionMessage(fullReduction);
                        cartPromotionItem.setPromotionMessage(message);
                        //(商品原价/总价)*满减金额
                        PmsSkuStock skuStock= getOriginalPrice(promotionProduct, item.getProductSkuId());
                        assert skuStock != null;
                        BigDecimal originalPrice = skuStock.getPrice();
                        BigDecimal reduceAmount = originalPrice.divide(totalAmount,RoundingMode.HALF_EVEN).multiply(fullReduction.getReducePrice());
                        cartPromotionItem.setReduceAmount(reduceAmount);
                        Integer lockStockVal3 = skuStock.getLockStock();
                        int lockStock3 = lockStockVal3 == null ? 0 : lockStockVal3;
                        cartPromotionItem.setRealStock(skuStock.getStock() - lockStock3);
                        cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
                        cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
                        cartPromotionItemList.add(cartPromotionItem);
                    }
                }else{
                    handleNoReduce(cartPromotionItemList,itemList,promotionProduct);
                }
            } else {
                //无优惠
                handleNoReduce(cartPromotionItemList, itemList,promotionProduct);
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

    /**
     * 获取满减促销消息
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
     * 对没满足优惠条件的商品进行处理
     */
    private void handleNoReduce(List<CartPromotionItem> cartPromotionItemList, List<OmsCartItem> itemList,PromotionProduct promotionProduct) {
        for (OmsCartItem item : itemList) {
            CartPromotionItem cartPromotionItem = new CartPromotionItem();
            BeanUtils.copyProperties(item,cartPromotionItem);
            cartPromotionItem.setPromotionMessage("无优惠");
            cartPromotionItem.setReduceAmount(new BigDecimal(0));
            PmsSkuStock skuStock = getOriginalPrice(promotionProduct,item.getProductSkuId());
            if(skuStock!=null){
                Integer lockStockVal4 = skuStock.getLockStock();
                int lockStock4 = lockStockVal4 == null ? 0 : lockStockVal4;
                cartPromotionItem.setRealStock(skuStock.getStock() - lockStock4);
            }
            cartPromotionItem.setIntegration(promotionProduct.getGiftPoint());
            cartPromotionItem.setGrowth(promotionProduct.getGiftGrowth());
            cartPromotionItemList.add(cartPromotionItem);
        }
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
     * 获取打折优惠的促销信息
     */
    private String getLadderPromotionMessage(PmsProductLadder ladder) {
        StringBuilder sb = new StringBuilder();
        sb.append("打折优惠：");
        sb.append("满");
        sb.append(ladder.getCount());
        sb.append("件，");
        sb.append("打");
        sb.append(ladder.getDiscount().multiply(new BigDecimal(10)));
        sb.append("折");
        return sb.toString();
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
            assert promotionProduct != null;
            PmsSkuStock skuStock = getOriginalPrice(promotionProduct,item.getProductSkuId());
            assert skuStock != null;
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