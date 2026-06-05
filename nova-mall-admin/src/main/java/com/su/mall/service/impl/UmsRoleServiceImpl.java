package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dao.UmsRoleDao;
import com.su.mall.mapper.UmsRoleMapper;
import com.su.mall.mapper.UmsRoleMenuRelationMapper;
import com.su.mall.mapper.UmsRoleResourceRelationMapper;
import com.su.mall.model.*;
import com.su.mall.service.UmsAdminCacheService;
import com.su.mall.service.UmsRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 后台角色管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class UmsRoleServiceImpl implements UmsRoleService {
    private final UmsRoleMapper roleMapper;
    private final UmsRoleMenuRelationMapper roleMenuRelationMapper;
    private final UmsRoleResourceRelationMapper roleResourceRelationMapper;
    private final UmsRoleDao roleDao;
    private final UmsAdminCacheService adminCacheService;
    @Override
    public int create(UmsRole role) {
        role.setCreateTime(new Date());
        role.setAdminCount(0);
        role.setSort(0);
        return roleMapper.insert(role);
    }

    @Override
    public int update(Long id, UmsRole role) {
        role.setId(id);
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        return roleMapper.updateById(role);
    }

    @Override
    public int delete(List<Long> ids) {
        // ✅ 改造：deleteByExample → delete + LambdaQueryWrapper
        int count = roleMapper.delete(new LambdaQueryWrapper<UmsRole>().in(UmsRole::getId, ids));
        adminCacheService.delResourceListByRoleIds(ids);
        return count;
    }

    @Override
    public List<UmsRole> list() {
        // ✅ 改造：selectByExample → selectList + LambdaQueryWrapper
        return roleMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public Page<UmsRole> list(String keyword, Integer pageSize, Integer pageNum) {
        Page<UmsRole> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList + LambdaQueryWrapper
        LambdaQueryWrapper<UmsRole> wrapper = new LambdaQueryWrapper<>();
        if (!StrUtil.isEmpty(keyword)) {
            wrapper.like(UmsRole::getName, keyword);
        }
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<UmsMenu> getMenuList(Long adminId) {
        return roleDao.getMenuList(adminId);
    }

    @Override
    public List<UmsMenu> listMenu(Long roleId) {
        return roleDao.getMenuListByRoleId(roleId);
    }

    @Override
    public List<UmsResource> listResource(Long roleId) {
        return roleDao.getResourceListByRoleId(roleId);
    }

    @Override
    public int allocMenu(Long roleId, List<Long> menuIds) {
        //先删除原有关系
        // ✅ 改造：deleteByExample → delete + LambdaQueryWrapper
        roleMenuRelationMapper.delete(new LambdaQueryWrapper<UmsRoleMenuRelation>().eq(UmsRoleMenuRelation::getRoleId, roleId));
        //批量插入新关系
        for (Long menuId : menuIds) {
            UmsRoleMenuRelation relation = new UmsRoleMenuRelation();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuRelationMapper.insert(relation);
        }
        return menuIds.size();
    }

    @Override
    public int allocResource(Long roleId, List<Long> resourceIds) {
        //先删除原有关系
        // ✅ 改造：deleteByExample → delete + LambdaQueryWrapper
        roleResourceRelationMapper.delete(new LambdaQueryWrapper<UmsRoleResourceRelation>().eq(UmsRoleResourceRelation::getRoleId, roleId));
        //批量插入新关系
        for (Long resourceId : resourceIds) {
            UmsRoleResourceRelation relation = new UmsRoleResourceRelation();
            relation.setRoleId(roleId);
            relation.setResourceId(resourceId);
            roleResourceRelationMapper.insert(relation);
        }
        adminCacheService.delResourceListByRole(roleId);
        return resourceIds.size();
    }
}
