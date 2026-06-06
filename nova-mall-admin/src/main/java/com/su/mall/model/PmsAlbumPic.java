package com.su.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PmsAlbumPic implements Serializable {
    private Long id;

    private Long albumId;

    private String pic;

    @Serial
    private static final long serialVersionUID = 1L;
}
