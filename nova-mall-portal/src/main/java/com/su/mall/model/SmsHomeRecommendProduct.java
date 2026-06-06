package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SmsHomeRecommendProduct implements Serializable {
    private Long id;

    private Long productId;

    private String productName;

    private Integer recommendStatus;

    private Integer sort;

    @Serial
    private static final long serialVersionUID = 1L;
}
