package com.su.mall.portal.domain.order;

import com.su.mall.model.OmsOrder;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.model.UmsMember;
import com.su.mall.portal.domain.CartPromotionItem;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单创建上下文（责任链模式的共享数据载体）
 * <p>在各 {@link OrderHandler} 处理器之间传递和共享订单创建过程中的数据
 * <p>包含当前会员、购物车商品、订单对象、订单商品列表等
 *
 * @see OrderCreationChain
 * @see OrderHandler
 */
@Getter
@Setter
public class OrderHandlerContext {

    /** 当前登录会员 */
    private UmsMember currentMember;

    /** 购物车促销商品列表（包含优惠信息） */
    private List<CartPromotionItem> cartPromotionItemList;

    /** 订单商品列表（由处理器逐步构建） */
    private List<OmsOrderItem> orderItemList = new ArrayList<>();

    /** 订单对象（由处理器逐步填充） */
    private OmsOrder order = new OmsOrder();

    /** 是否包含秒杀商品 */
    private boolean hasFlashPromotion = false;

    /** 扩展属性容器（处理器间传递额外数据） */
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * 设置扩展属性
     *
     * @param key   属性键
     * @param value 属性值
     */
    public void putAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 获取扩展属性（自动类型转换）
     *
     * @param key 属性键
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }
}
