package com.su.mall.portal.domain;

import com.su.mall.model.PmsProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 商品分类树节点（树形结构）
 * <p>继承商品分类基础信息，扩展子分类列表，形成递归树结构
 * <p>用于首页分类导航和商品搜索页的分类筛选
 *
 * @see com.su.mall.portal.service.PmsPortalProductService#categoryTreeList()
 */
@Getter
@Setter
public class PmsProductCategoryNode extends PmsProductCategory {

    @Schema(title = "子分类集合（递归嵌套）")
    private List<PmsProductCategoryNode> children;
}
