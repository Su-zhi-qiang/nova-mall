package com.su.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.model.UmsResource;

import java.util.List;

/**
 * 后台资源管理Service
 * <p>资源（API接口）是动态权限控制的基础单元
 * <p>每个资源关联一个URL路径，角色通过资源关联获得对应接口的访问权限
 * <p>listAll() 被 {@link com.su.mall.security.component.DynamicSecurityService} 调用，构建运行时URL→权限映射
 *
 * @see UmsResourceServiceImpl
 */
public interface UmsResourceService {
    /**
     * 添加资源
     */
    int create(UmsResource umsResource);

    /**
     * 修改资源
     */
    int update(Long id, UmsResource umsResource);

    /**
     * 获取资源详情
     */
    UmsResource getItem(Long id);

    /**
     * 删除资源
     */
    int delete(Long id);

    /**
     * 分页查询资源
     */
    Page<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword, Integer pageSize, Integer pageNum);

    /**
     * 查询全部资源
     */
    List<UmsResource> listAll();
}
