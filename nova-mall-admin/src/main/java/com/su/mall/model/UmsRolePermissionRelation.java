package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色权限关系
 * @author Su
 */
@Data
public class UmsRolePermissionRelation implements Serializable {
    private Long id;

    private Long roleId;

    private Long permissionId;

    @Serial
    private static final long serialVersionUID = 1L;
}
