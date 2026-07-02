package com.su.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dto.UmsMenuNode;
import com.su.mall.model.UmsMenu;

import java.util.List;

/**
 * 后台菜单管理Service
 * <p>管理后台前端菜单树（侧边栏导航），支持树形结构查询
 * <p>菜单通过角色菜单关系表（UmsRoleMenuRelation）与角色关联，控制前端页面可见性
 *
 * @see UmsMenuServiceImpl
 */
public interface UmsMenuService {
    /**
     * 创建后台菜单
     */
    int create(UmsMenu umsMenu);

    /**
     * 修改后台菜单
     */
    int update(Long id, UmsMenu umsMenu);

    /**
     * 根据ID获取菜单详情
     */
    UmsMenu getItem(Long id);

    /**
     * 根据ID删除菜单
     */
    int delete(Long id);

    /**
     * 分页查询后台菜单
     */
    Page<UmsMenu> list(Long parentId, Integer pageSize, Integer pageNum);

    /**
     * 树形结构返回所有菜单列表
     */
    List<UmsMenuNode> treeList();

    /**
     * 修改菜单显示状态
     */
    int updateHidden(Long id, Integer hidden);
}
