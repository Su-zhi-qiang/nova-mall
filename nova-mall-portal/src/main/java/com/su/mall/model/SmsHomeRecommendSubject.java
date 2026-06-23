package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 首页推荐专题
 * @author Su
 */
@Data
public class SmsHomeRecommendSubject implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long subjectId;

    private String subjectName;

    private Integer recommendStatus;

    private Integer sort;
}
