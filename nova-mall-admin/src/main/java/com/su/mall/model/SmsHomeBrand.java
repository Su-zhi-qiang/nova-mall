package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 首页推荐品牌
 * @author Su
 */
@Data
public class SmsHomeBrand implements Serializable {
    private Long id;

    private Long brandId;

    private String brandName;

    private Integer recommendStatus;

    private Integer sort;

    @Serial
    private static final long serialVersionUID = 1L;
}
