package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dto.UmsMenuNode;
import com.su.mall.mapper.UmsMenuMapper;
import com.su.mall.model.*;
import com.su.mall.service.UmsMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台菜单管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class UmsMenuServiceImpl implements UmsMenuService {
    private final UmsMenuMapper menuMapper;

    @Override
    @Transactional
    public int create(UmsMenu umsMenu) {
        umsMenu.setCreateTime(new Date());
        updateLevel(umsMenu);
        return menuMapper.insert(umsMenu);
    }

    /**
     * 修改菜单层级
     */
    private void updateLevel(UmsMenu umsMenu) {
        if (umsMenu.getParentId() == 0) {
            //没有父菜单时为一级菜单
            umsMenu.setLevel(0);
        } else {
            //有父菜单时选择根据父菜单level设置
            UmsMenu parentMenu = menuMapper.selectById(umsMenu.getParentId());
            if (parentMenu != null) {
                umsMenu.setLevel(parentMenu.getLevel() + 1);
            } else {
                umsMenu.setLevel(0);
            }
        }
    }

    @Override
    @Transactional
    public int update(Long id, UmsMenu umsMenu) {
        umsMenu.setId(id);
        updateLevel(umsMenu);
        return menuMapper.updateById(umsMenu);
    }

    @Override
    public UmsMenu getItem(Long id) {
        return menuMapper.selectById(id);
    }

    @Override
    @Transactional
    public int delete(Long id) {
        return menuMapper.deleteById(id);
    }

    @Override
    public Page<UmsMenu> list(Long parentId, Integer pageSize, Integer pageNum) {
        Page<UmsMenu> page = new Page<>(pageNum, pageSize);
        return menuMapper.selectPage(page, new LambdaQueryWrapper<UmsMenu>()
                .eq(UmsMenu::getParentId, parentId)
                .orderByDesc(UmsMenu::getSort));
    }

    @Override
    public List<UmsMenuNode> treeList() {
        List<UmsMenu> menuList = menuMapper.selectList(new LambdaQueryWrapper<>());
        List<UmsMenuNode> result = menuList.stream()
                .filter(menu -> menu.getParentId().equals(0L))
                .map(menu -> covertMenuNode(menu, menuList))
                .collect(Collectors.toList());
        return result;
    }

    @Override
    @Transactional
    public int updateHidden(Long id, Integer hidden) {
        UmsMenu umsMenu = new UmsMenu();
        umsMenu.setId(id);
        umsMenu.setHidden(hidden);
        return menuMapper.updateById(umsMenu);
    }

    /**
     * 将UmsMenu转化为UmsMenuNode并设置children属性
     */
    private UmsMenuNode covertMenuNode(UmsMenu menu, List<UmsMenu> menuList) {
        UmsMenuNode node = new UmsMenuNode();
        BeanUtils.copyProperties(menu, node);
        List<UmsMenuNode> children = menuList.stream()
                .filter(subMenu -> subMenu.getParentId().equals(menu.getId()))
                .map(subMenu -> covertMenuNode(subMenu, menuList)).collect(Collectors.toList());
        node.setChildren(children);
        return node;
    }
}
