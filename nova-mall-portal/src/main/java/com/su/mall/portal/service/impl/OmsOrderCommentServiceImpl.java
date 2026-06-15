package com.su.mall.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.common.exception.Asserts;
import com.su.mall.mapper.OmsOrderCommentMapper;
import com.su.mall.mapper.OmsOrderItemMapper;
import com.su.mall.model.OmsOrderComment;
import com.su.mall.model.OmsOrderItem;
import com.su.mall.portal.domain.OmsOrderCommentParam;
import com.su.mall.portal.service.UmsMemberService;
import com.su.mall.portal.service.OmsOrderCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品评价Service实现类
 */
@Service
@RequiredArgsConstructor
public class OmsOrderCommentServiceImpl implements OmsOrderCommentService {
    private final OmsOrderCommentMapper commentMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final UmsMemberService memberService;

    @Override
    @Transactional
    public int create(OmsOrderCommentParam param) {
        Long memberId = memberService.getCurrentMember().getId();
        String memberNickName = memberService.getCurrentMember().getNickname();

        OmsOrderItem orderItem = orderItemMapper.selectById(param.getOrderItemId());
        if (orderItem == null) {
            Asserts.fail("订单项不存在");
        }

        if (orderItem.getIsCommented() != null && orderItem.getIsCommented() == 1) {
            Asserts.fail("该商品已评价，请勿重复提交");
        }

        OmsOrderComment comment = new OmsOrderComment();
        comment.setOrderId(orderItem.getOrderId());
        comment.setOrderItemId(orderItem.getId());
        comment.setProductId(orderItem.getProductId());
        comment.setMemberId(memberId);
        comment.setMemberNickName(memberNickName);
        comment.setStar(param.getStar());
        comment.setContent(param.getContent());
        comment.setPics(param.getPics());
        comment.setShowStatus(1);
        comment.setCreateTime(new Date());

        int result = commentMapper.insert(comment);

        orderItem.setIsCommented(1);
        orderItemMapper.updateById(orderItem);

        return result;
    }

    @Override
    public Map<String, Object> listByProductId(Long productId) {
        // 查询评价列表
        List<OmsOrderComment> comments = commentMapper.selectList(
            new LambdaQueryWrapper<OmsOrderComment>()
                .eq(OmsOrderComment::getProductId, productId)
                .eq(OmsOrderComment::getShowStatus, 1)
                .orderByDesc(OmsOrderComment::getCreateTime)
                .last("LIMIT 10"));

        // 统计评价总数
        Long totalCount = commentMapper.selectCount(
            new LambdaQueryWrapper<OmsOrderComment>()
                .eq(OmsOrderComment::getProductId, productId)
                .eq(OmsOrderComment::getShowStatus, 1));

        // 计算好评率（star >= 4 为好评）
        Long goodCount = commentMapper.selectCount(
            new LambdaQueryWrapper<OmsOrderComment>()
                .eq(OmsOrderComment::getProductId, productId)
                .eq(OmsOrderComment::getShowStatus, 1)
                .ge(OmsOrderComment::getStar, 4));

        int goodRate = totalCount > 0 ? (int) (goodCount * 100 / totalCount) : 100;

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("goodRate", goodRate);
        result.put("list", comments);
        return result;
    }
}
