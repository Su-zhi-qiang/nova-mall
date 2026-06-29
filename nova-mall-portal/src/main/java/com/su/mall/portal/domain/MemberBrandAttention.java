package com.su.mall.portal.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 会员品牌关注记录（MongoDB文档）
 * <p>存储在MongoDB中，记录会员对品牌的关注关系
 * <p> memberId 和 brandId 建立索引以支持高效查询
 *
 * @see com.su.mall.portal.service.MemberAttentionService
 */
@Getter
@Setter
@Document
public class MemberBrandAttention {

    @Id
    private String id;

    @Indexed
    private Long memberId;

    private String memberNickname;

    private String memberIcon;

    @Indexed
    private Long brandId;

    private String brandName;

    private String brandLogo;

    private String brandCity;

    private Date createTime;
}
