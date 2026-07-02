package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 话题分类
 * 
 */
@Data
public class CmsTopicCategory implements Serializable {
    private Long id;

    private String name;

    @Schema(title = "分类图标")
    private String icon;

    @Schema(title = "专题数量")
    private Integer subjectCount;

    private Integer showStatus;

    private Integer sort;

    @Serial
    private static final long serialVersionUID = 1L;
}