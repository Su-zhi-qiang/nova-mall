package com.su.mall.portal.dao;

import com.su.mall.model.OmsOrderItem;
import com.su.mall.portal.domain.OmsOrderDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 前台订单管理自定义Dao
 * @author Su
 */
public interface PortalOrderDao {
    /**
     * 获取订单及下单商品详情
     */
    OmsOrderDetail getDetail(@Param("orderId") Long orderId);

    /**
     * 修改 pms_sku_stock表的锁定库存及真实库存
     */
    int updateSkuStock(@Param("itemList") List<OmsOrderItem> orderItemList);

    /**
     * 获取超时订单
     * @param minute 超时时间（分）
     */
    List<OmsOrderDetail> getTimeOutOrders(@Param("minute") Integer minute);

    /**
     * 批量修改订单状态
     */
    int updateOrderStatus(@Param("ids") List<Long> ids,@Param("status") Integer status);

    /**
     * 解除取消订单的库存锁定
     */
    int releaseSkuStockLock(@Param("itemList") List<OmsOrderItem> orderItemList);

    /**
     * 更新商品销量
     */
    int updateProductSale(@Param("itemList") List<OmsOrderItem> orderItemList);

    /**
     * 更新SKU销量
     */
    int updateSkuSale(@Param("itemList") List<OmsOrderItem> orderItemList);

    /**
     * 查询会员在某秒杀场次中已购买的数量（已支付状态）
     */
    int getMemberFlashBuyCount(@Param("memberId") Long memberId, @Param("relationId") Long relationId);

}
