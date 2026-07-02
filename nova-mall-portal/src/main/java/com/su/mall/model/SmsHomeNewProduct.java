package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 首页新品推荐
 * 
 */
@Data
public class SmsHomeNewProduct implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long productId;

    private String productName;

    private Integer recommendStatus;

    private Integer sort;
}
