package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.io.Serial;
import java.util.Date;
import lombok.Data;

@Data
public class CmsTopicComment implements Serializable {
    private Long id;

    private String memberNickName;

    private Long topicId;

    private String memberIcon;

    private String content;

    private Date createTime;

    private Integer showStatus;

    @Serial
    private static final long serialVersionUID = 1L;
}
