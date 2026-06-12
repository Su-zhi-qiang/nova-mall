package com.su.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.model.*;
import com.su.mall.service.UmsRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台用户角色管理Controller
 * @author Su
 */
@RestController
@Tag(name = "UmsRoleController", description = "后台用户角色管理")
@RequestMapping("/role")
@RequiredArgsConstructor
public class UmsRoleController {
    private final UmsRoleService roleService;

    @Operation(summary = "添加角色")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> create(@RequestBody UmsRole role) {
        int count = roleService.create(role);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改角色")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody UmsRole role) {
        int count = roleService.update(id, role);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "批量删除角色")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<Integer> delete(@RequestParam("ids") List<Long> ids) {
        int count = roleService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取所有角色")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public CommonResult<List<UmsRole>> listAll() {
        List<UmsRole> roleList = roleService.list();
        return CommonResult.success(roleList);
    }

    @Operation(summary = "根据角色名称分页获取角色列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UmsRole>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                  @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<UmsRole> rolePage = roleService.list(keyword, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(rolePage));
    }

    @Operation(summary = "修改角色状态")
    @RequestMapping(value = "/updateStatus/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> updateStatus(@PathVariable Long id, @RequestParam(value = "status") Integer status) {
        UmsRole umsRole = new UmsRole();
        umsRole.setStatus(status);
        int count = roleService.update(id, umsRole);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取角色相关菜单")
    @RequestMapping(value = "/listMenu/{roleId}", method = RequestMethod.GET)
    public CommonResult<List<UmsMenu>> listMenu(@PathVariable Long roleId) {
        List<UmsMenu> roleList = roleService.listMenu(roleId);
        return CommonResult.success(roleList);
    }

    @Operation(summary = "获取角色相关资源")
    @RequestMapping(value = "/listResource/{roleId}", method = RequestMethod.GET)
    public CommonResult<List<UmsResource>> listResource(@PathVariable Long roleId) {
        List<UmsResource> roleList = roleService.listResource(roleId);
        return CommonResult.success(roleList);
    }

    @Operation(summary = "给角色分配菜单")
    @RequestMapping(value = "/allocMenu", method = RequestMethod.POST)
    public CommonResult<Integer> allocMenu(@RequestParam Long roleId, @RequestParam List<Long> menuIds) {
        int count = roleService.allocMenu(roleId, menuIds);
        return CommonResult.success(count);
    }

    @Operation(summary = "给角色分配资源")
    @RequestMapping(value = "/allocResource", method = RequestMethod.POST)
    public CommonResult<Integer> allocResource(@RequestParam Long roleId, @RequestParam List<Long> resourceIds) {
        int count = roleService.allocResource(roleId, resourceIds);
        return CommonResult.success(count);
    }

}
