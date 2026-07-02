package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsHomeAdvertiseMapper;
import com.su.mall.model.SmsHomeAdvertise;
import com.su.mall.service.SmsHomeAdvertiseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 首页广告管理Service实现类
 *
 * 
 */
@Service
@RequiredArgsConstructor
public class SmsHomeAdvertiseServiceImpl implements SmsHomeAdvertiseService {
    private final SmsHomeAdvertiseMapper advertiseMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private void clearHomeContentCache() {
        try {
            redisTemplate.delete("home:content");
        } catch (Exception e) {
            // 缓存清除失败不影响业务
        }
    }

    @Override
    public int create(SmsHomeAdvertise advertise) {
        advertise.setClickCount(0);
        advertise.setOrderCount(0);
        int result = advertiseMapper.insert(advertise);
        clearHomeContentCache();
        return result;
    }

    @Override
    public int delete(List<Long> ids) {
        int result = advertiseMapper.delete(new LambdaQueryWrapper<SmsHomeAdvertise>()
                .in(SmsHomeAdvertise::getId, ids));
        clearHomeContentCache();
        return result;
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        SmsHomeAdvertise record = new SmsHomeAdvertise();
        record.setId(id);
        record.setStatus(status);
        int result = advertiseMapper.updateById(record);
        clearHomeContentCache();
        return result;
    }

    @Override
    public SmsHomeAdvertise getItem(Long id) {
        return advertiseMapper.selectById(id);
    }

    @Override
    public int update(Long id, SmsHomeAdvertise advertise) {
        advertise.setId(id);
        int result = advertiseMapper.updateById(advertise);
        clearHomeContentCache();
        return result;
    }

    @Override
    public Page<SmsHomeAdvertise> list(String name, Integer type, String endTime, Integer pageSize, Integer pageNum) {
        Page<SmsHomeAdvertise> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsHomeAdvertise> wrapper = new LambdaQueryWrapper<>();
        SmsHomeAdvertise temp = new SmsHomeAdvertise();
        if (!StrUtil.isEmpty(name)) {
            wrapper.like(SmsHomeAdvertise::getName, name);
        }
        if (type != null) {
            wrapper.eq(SmsHomeAdvertise::getType, type);
        }
        if (!StrUtil.isEmpty(endTime)) {
            String startStr = endTime + " 00:00:00";
            String endStr = endTime + " 23:59:59";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = null;
            Date end = null;
            try {
                start = sdf.parse(startStr);
                end = sdf.parse(endStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            wrapper.ge(SmsHomeAdvertise::getStartTime, start);
            wrapper.le(SmsHomeAdvertise::getEndTime, end);
        }
        wrapper.orderByDesc(SmsHomeAdvertise::getSort);
        return advertiseMapper.selectPage(page, wrapper);
    }
}
