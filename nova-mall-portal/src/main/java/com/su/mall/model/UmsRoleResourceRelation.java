package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色资源关系
 * @author Su
 */
@Data
public class UmsRoleResourceRelation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(title = "角色ID")
    private Long roleId;

    @Schema(title = "资源ID")
    private Long resourceId;
}
