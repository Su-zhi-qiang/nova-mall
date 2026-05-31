package com.su.mall.dto;

import com.su.mall.model.SmsFlashPromotionSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 包含商品数量的场次信息
 * @author Su
 */
public class SmsFlashPromotionSessionDetail extends SmsFlashPromotionSession {
    @Setter
    @Getter
    @Schema(title = "商品数量")
    private Long productCount;
}
