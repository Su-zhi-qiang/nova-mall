package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 商品评价回复
 * 
 */
@Data
public class PmsCommentReplay implements Serializable {
    private Long id;

    private Long commentId;

    private String memberNickName;

    private String memberIcon;

    private String content;

    private Date createTime;

    @Schema(title = "评论人员类型；0->会员；1->管理员")
    private Integer type;

    @Serial
    private static final long serialVersionUID = 1L;
}
