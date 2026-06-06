package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UmsMenu implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @Schema(title = "父级ID")
    private Long parentId;

    @Schema(title = "创建时间")
    private Date createTime;

    @Schema(title = "菜单名称")
    private String title;

    @Schema(title = "菜单级数")
    private Integer level;

    @Schema(title = "菜单排序")
    private Integer sort;

    @Schema(title = "前端名称")
    private String name;

    @Schema(title = "前端图标")
    private String icon;

    @Schema(title = "前端隐藏")
    private Integer hidden;
}
