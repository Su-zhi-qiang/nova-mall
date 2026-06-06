package com.su.mall.dto;

import com.su.mall.model.PmsProduct;
import com.su.mall.model.SmsFlashPromotionProductRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 限时购商品信息封装
 * @author Su
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SmsFlashPromotionProduct extends SmsFlashPromotionProductRelation{
    @Schema(title = "关联商品")
    private PmsProduct product;
}
