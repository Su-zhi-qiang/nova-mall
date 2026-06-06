package com.su.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.su.mall.model.PmsProductOperateLog;

/**
 * 商品操作日志Service接口
 */
public interface PmsProductOperateLogService {

    /**
     * 根据商品ID分页查询操作日志
     */
    IPage<PmsProductOperateLog> listByProductId(Long productId, Integer pageNum, Integer pageSize);

    /**
     * 保存操作日志
     */
    void saveLog(PmsProductOperateLog log);
}