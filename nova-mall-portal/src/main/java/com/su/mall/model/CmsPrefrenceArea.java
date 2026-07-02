package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 优选专区
 * 
 */
@Data
public class CmsPrefrenceArea implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String subTitle;

    private Integer sort;

    private Integer showStatus;

    @Schema(title = "展示图片")
    private byte[] pic;
}
