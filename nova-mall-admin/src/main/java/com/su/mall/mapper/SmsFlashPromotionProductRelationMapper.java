package com.su.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SmsFlashPromotionProductRelationMapper extends BaseMapper<SmsFlashPromotionProductRelation> {

    @Update("UPDATE sms_flash_promotion_product_relation " +
            "SET flash_promotion_count = flash_promotion_count + #{quantity}, " +
            "flash_promotion_sold = GREATEST(COALESCE(flash_promotion_sold, 0) - #{quantity}, 0) " +
            "WHERE id = #{relationId}")
    int restoreStock(@Param("relationId") Long relationId, @Param("quantity") Integer quantity);

    @Update("UPDATE sms_flash_promotion_product_relation " +
            "SET flash_promotion_count = flash_promotion_count - #{quantity}, " +
            "flash_promotion_sold = COALESCE(flash_promotion_sold, 0) + #{quantity} " +
            "WHERE id = #{relationId} " +
            "AND flash_promotion_count >= #{quantity}")
    int decreaseStock(@Param("relationId") Long relationId, @Param("quantity") Integer quantity);

    @Update("UPDATE sms_flash_promotion_product_relation " +
            "SET flash_promotion_count = original_count, " +
            "flash_promotion_sold = 0 " +
            "WHERE flash_promotion_id = #{flashPromotionId} " +
            "AND flash_promotion_session_id = #{flashPromotionSessionId} " +
            "AND original_count IS NOT NULL")
    int resetFlashStock(@Param("flashPromotionId") Long flashPromotionId,
                        @Param("flashPromotionSessionId") Long flashPromotionSessionId);
}
