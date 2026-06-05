package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CmsPrefrenceArea implements Serializable {
    private Long id;

    private String name;

    private String subTitle;

    private Integer sort;

    private Integer showStatus;

    @Schema(title = "展示图片")
    private byte[] pic;

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", subTitle=").append(subTitle);
        sb.append(", sort=").append(sort);
        sb.append(", showStatus=").append(showStatus);
        sb.append(", pic=").append(pic);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}