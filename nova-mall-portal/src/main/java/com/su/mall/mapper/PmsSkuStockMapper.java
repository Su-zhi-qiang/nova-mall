package com.su.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.su.mall.model.PmsSkuStock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface PmsSkuStockMapper extends BaseMapper<PmsSkuStock> {

    @Update("UPDATE pms_sku_stock SET lock_stock = lock_stock + #{quantity} WHERE id = #{id} AND stock - lock_stock >= #{quantity}")
    int increaseLockStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Update("UPDATE pms_sku_stock SET lock_stock = lock_stock - #{quantity} WHERE id = #{id}")
    int decreaseLockStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
