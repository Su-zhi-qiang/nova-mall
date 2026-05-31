package com.su.mall.portal.dao;

import com.su.mall.model.OmsOrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单商品信息管理自定义Dao
 * @author Su
 */
public interface PortalOrderItemDao {
    /**
     * 批量插入
     */
    int insertList(@Param("list") List<OmsOrderItem> list);
}
