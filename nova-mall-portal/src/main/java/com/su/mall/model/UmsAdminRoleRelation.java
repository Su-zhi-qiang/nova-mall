package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员角色关系
 * @author Su
 */
@Data
public class UmsAdminRoleRelation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long adminId;

    private Long roleId;
}
