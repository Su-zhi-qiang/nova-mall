package com.su.mall.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dto.SmsFlashPromotionProduct;
import org.apache.ibatis.annotations.Param;

/**
 * 限时购商品关系管理自定义Dao
 * 
 */
public interface SmsFlashPromotionProductRelationDao {
    /**
     * 获取限时购及相关商品信息（支持 MyBatis-Plus 分页）
     */
    Page<SmsFlashPromotionProduct> getList(Page<?> page,
                                           @Param("flashPromotionId") Long flashPromotionId,
                                           @Param("flashPromotionSessionId") Long flashPromotionSessionId);
}
