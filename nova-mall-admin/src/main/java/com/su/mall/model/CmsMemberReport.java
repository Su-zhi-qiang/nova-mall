package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户举报
 * 
 */
@Data
public class CmsMemberReport implements Serializable {
    private Long id;

    @Schema(title = "举报类型：0->商品评价；1->话题内容；2->用户评论")
    private Integer reportType;

    @Schema(title = "举报人")
    private String reportMemberName;

    private Date createTime;

    private String reportObject;

    @Schema(title = "举报状态：0->未处理；1->已处理")
    private Integer reportStatus;

    @Schema(title = "处理结果：0->无效；1->有效；2->恶意")
    private Integer handleStatus;

    private String note;

    @Serial
    private static final long serialVersionUID = 1L;
}
