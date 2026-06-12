package com.su.mall.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dto.OmsOrderDeliveryParam;
import com.su.mall.dto.OmsOrderDetail;
import com.su.mall.dto.OmsOrderQueryParam;
import com.su.mall.model.OmsOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单查询自定义Dao
 * @author Su
 */
public interface OmsOrderDao {
    /**
     * 条件分页查询订单
     */
    Page<OmsOrder> getList(Page<?> page, @Param("queryParam") OmsOrderQueryParam queryParam);

    /**
     * 批量发货
     */
    int delivery(@Param("list") List<OmsOrderDeliveryParam> deliveryParamList);

    /**
     * 获取订单详情（订单信息 + 商品列表 + 操作记录）
     */
    OmsOrderDetail getDetail(@Param("id") Long id);
}
