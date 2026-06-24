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
 * 订单创建上下文，在责任链处理器之间共享数据
 */
@Getter
@Setter
public class OrderHandlerContext {

    private UmsMember currentMember;
    private List<CartPromotionItem> cartPromotionItemList;
    private List<OmsOrderItem> orderItemList = new ArrayList<>();
    private OmsOrder order = new OmsOrder();
    private boolean hasFlashPromotion = false;

    private final Map<String, Object> attributes = new HashMap<>();

    public void putAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }
}