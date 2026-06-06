package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.PmsProductOperateLogMapper;
import com.su.mall.model.PmsProductOperateLog;
import com.su.mall.service.PmsProductOperateLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 商品操作日志Service实现类
 */
@Service
@RequiredArgsConstructor
public class PmsProductOperateLogServiceImpl implements PmsProductOperateLogService {

    private final PmsProductOperateLogMapper productOperateLogMapper;

    @Override
    public IPage<PmsProductOperateLog> listByProductId(Long productId, Integer pageNum, Integer pageSize) {
        Page<PmsProductOperateLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PmsProductOperateLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProductOperateLog::getProductId, productId)
                .orderByDesc(PmsProductOperateLog::getCreateTime);
        return productOperateLogMapper.selectPage(page, wrapper);
    }

    @Override
    public void saveLog(PmsProductOperateLog log) {
        productOperateLogMapper.insert(log);
    }
}