package com.su.mall.portal.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 会员商品收藏记录（MongoDB文档）
 * <p>存储在MongoDB中，记录会员对商品的收藏关系
 * <p> memberId 和 productId 建立索引以支持高效查询
 *
 * @see com.su.mall.portal.service.MemberCollectionService
 */
@Getter
@Setter
@Document
public class MemberProductCollection {

    @Id
    private String id;

    @Indexed
    private Long memberId;

    private String memberNickname;

    private String memberIcon;

    @Indexed
    private Long productId;

    private String productName;

    private String productPic;

    private String productSubTitle;

    private String productPrice;

    private Date createTime;
}
