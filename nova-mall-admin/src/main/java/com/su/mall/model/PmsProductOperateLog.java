package com.su.mall.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品操作日志
 * @author Su
 */
@Data
@TableName("pms_product_operate_log")
public class PmsProductOperateLog implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private BigDecimal priceOld;

    private BigDecimal priceNew;

    private BigDecimal salePriceOld;

    private BigDecimal salePriceNew;

    @Schema(title = "赠送的积分")
    private Integer giftPointOld;

    private Integer giftPointNew;

    private Integer usePointLimitOld;

    private Integer usePointLimitNew;

    @Schema(title = "操作人")
    private String operateMan;

    private Date createTime;

    @Schema(title = "旧审核状态")
    private Integer verifyStatusOld;

    @Schema(title = "新审核状态")
    private Integer verifyStatusNew;

    @Schema(title = "旧审核备注")
    private String verifyRemarkOld;

    @Schema(title = "新审核备注")
    private String verifyRemarkNew;

    @Serial
    private static final long serialVersionUID = 1L;
}
