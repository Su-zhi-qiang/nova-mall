package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UmsAdminPermissionRelation implements Serializable {
    private Long id;

    private Long adminId;

    private Long permissionId;

    private Integer type;

    @Serial
    private static final long serialVersionUID = 1L;
}
