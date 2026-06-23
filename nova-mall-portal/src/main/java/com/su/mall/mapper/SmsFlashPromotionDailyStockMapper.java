package com.su.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.su.mall.model.SmsFlashPromotionDailyStock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface SmsFlashPromotionDailyStockMapper extends BaseMapper<SmsFlashPromotionDailyStock> {

    @Update("UPDATE sms_flash_promotion_daily_stock " +
            "SET stock = stock - #{quantity}, sold = sold + #{quantity} " +
            "WHERE relation_id = #{relationId} " +
            "AND batch_date = CURDATE() " +
            "AND stock >= #{quantity}")
    int decreaseStock(@Param("relationId") Long relationId, @Param("quantity") Integer quantity);

    @Update("UPDATE sms_flash_promotion_daily_stock " +
            "SET stock = stock + #{quantity}, sold = GREATEST(sold - #{quantity}, 0) " +
            "WHERE relation_id = #{relationId} " +
            "AND batch_date = CURDATE() " +
            "AND stock + #{quantity} <= (SELECT original_count FROM sms_flash_promotion_product_relation WHERE id = #{relationId})")
    int restoreStock(@Param("relationId") Long relationId, @Param("quantity") Integer quantity);

    @Select("SELECT stock FROM sms_flash_promotion_daily_stock " +
            "WHERE relation_id = #{relationId} AND batch_date = CURDATE()")
    Integer getCurrentStock(@Param("relationId") Long relationId);

    @Select("SELECT stock, sold FROM sms_flash_promotion_daily_stock " +
            "WHERE relation_id = #{relationId} AND batch_date = CURDATE()")
    SmsFlashPromotionDailyStock getDailyStock(@Param("relationId") Long relationId);
}
