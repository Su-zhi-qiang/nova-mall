package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 帮助信息
 * 
 */
@Data
public class CmsHelp implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long categoryId;

    private String icon;

    private String title;

    private Integer showStatus;

    private Date createTime;

    private Integer readCount;

    private String content;
}
