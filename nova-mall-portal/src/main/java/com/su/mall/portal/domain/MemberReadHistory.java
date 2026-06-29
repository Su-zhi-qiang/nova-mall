package com.su.mall.portal.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 会员商品浏览历史记录（MongoDB文档）
 * <p>存储在MongoDB中，记录会员浏览商品的历史
 * <p> memberId 和 productId 建立索引，按创建时间倒序排列
 *
 * @see com.su.mall.portal.service.MemberReadHistoryService
 */
@Getter
@Setter
@Document
public class MemberReadHistory {

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
