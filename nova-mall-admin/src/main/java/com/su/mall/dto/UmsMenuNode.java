package com.su.mall.dto;

import com.su.mall.model.UmsMenu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 后台菜单节点封装
 * @author Su
 */
@Getter
@Setter
public class UmsMenuNode extends UmsMenu {
    @Schema(title =  "子级菜单")
    private List<UmsMenuNode> children;
}
