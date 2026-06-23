package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 管理员登录日志
 * @author Su
 */
@Data
public class UmsAdminLoginLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long adminId;

    private Date createTime;

    private String ip;

    private String address;

    @Schema(title = "浏览器登录类型")
    private String userAgent;
}
