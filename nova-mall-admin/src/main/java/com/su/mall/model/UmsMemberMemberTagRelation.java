package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UmsMemberMemberTagRelation implements Serializable {
    private Long id;

    private Long memberId;

    private Long tagId;

    @Serial
    private static final long serialVersionUID = 1L;
}
