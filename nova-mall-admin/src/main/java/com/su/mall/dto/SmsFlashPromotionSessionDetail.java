package com.su.mall.dto;

import com.su.mall.model.SmsFlashPromotionSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 包含商品数量的场次信息
 * 
 */
@EqualsAndHashCode(callSuper = true) // 调用父类的equals方法
@Data
public class SmsFlashPromotionSessionDetail extends SmsFlashPromotionSession {

    @Schema(title = "商品数量")
    private Long productCount;
}
