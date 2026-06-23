package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色权限关系
 * @author Su
 */
@Data
public class UmsRolePermissionRelation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long roleId;

    private Long permissionId;
}
