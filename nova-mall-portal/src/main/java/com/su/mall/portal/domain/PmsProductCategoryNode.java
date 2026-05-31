package com.su.mall.portal.domain;

import com.su.mall.model.PmsProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 包含子分类的商品分类
 * @author Su
 */
@Getter
@Setter
public class PmsProductCategoryNode extends PmsProductCategory {
    @Schema(title = "子分类集合")
    private List<PmsProductCategoryNode> children;
}
