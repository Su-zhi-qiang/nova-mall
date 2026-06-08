package com.su.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SmsFlashPromotionProductRelationMapper extends BaseMapper<SmsFlashPromotionProductRelation> {

    @Update("UPDATE sms_flash_promotion_product_relation " +
            "SET flash_promotion_count = flash_promotion_count - #{quantity} " +
            "WHERE id = #{relationId} " +
            "AND flash_promotion_count >= #{quantity}")
    int decreaseStock(@Param("relationId") Long relationId, @Param("quantity") Integer quantity);
}
