package com.su.mall.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("pms_product_vertify_record")
// 商品审核记录, 实现Serializable接口是为了方便序列化
public class PmsProductVerifyRecord implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("verify_man")
    @Schema(title = "审核人")
    private String verifyMan;

    @TableField("status")
    private Integer status;

    @TableField("detail")
    @Schema(title = "反馈详情")
    private String detail;

    @Serial
    private static final long serialVersionUID = 1L;
}
