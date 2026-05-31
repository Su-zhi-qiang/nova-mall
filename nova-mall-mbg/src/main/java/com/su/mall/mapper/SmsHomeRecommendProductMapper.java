package com.su.mall.mapper;

import com.su.mall.model.SmsHomeRecommendProduct;
import com.su.mall.model.SmsHomeRecommendProductExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SmsHomeRecommendProductMapper {
    long countByExample(SmsHomeRecommendProductExample example);

    int deleteByExample(SmsHomeRecommendProductExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsHomeRecommendProduct row);

    int insertSelective(SmsHomeRecommendProduct row);

    List<SmsHomeRecommendProduct> selectByExample(SmsHomeRecommendProductExample example);

    SmsHomeRecommendProduct selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") SmsHomeRecommendProduct row, @Param("example") SmsHomeRecommendProductExample example);

    int updateByExample(@Param("row") SmsHomeRecommendProduct row, @Param("example") SmsHomeRecommendProductExample example);

    int updateByPrimaryKeySelective(SmsHomeRecommendProduct row);

    int updateByPrimaryKey(SmsHomeRecommendProduct row);
}