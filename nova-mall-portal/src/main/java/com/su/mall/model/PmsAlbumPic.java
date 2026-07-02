package com.su.mall.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品相册图片
 * 
 */
@Data
public class PmsAlbumPic implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long albumId;

    private String pic;
}
