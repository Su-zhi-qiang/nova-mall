package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 首页推荐品牌
 * 
 */
@Data
public class SmsHomeBrand implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long brandId;

    private String brandName;

    private Integer recommendStatus;

    private Integer sort;
}
