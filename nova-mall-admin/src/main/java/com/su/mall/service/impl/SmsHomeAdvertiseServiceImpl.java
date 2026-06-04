package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.SmsHomeAdvertiseMapper;
import com.su.mall.model.SmsHomeAdvertise;
import com.su.mall.service.SmsHomeAdvertiseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 首页广告管理Service实现类
 *
 * @author Su
 */
@Service
public class SmsHomeAdvertiseServiceImpl implements SmsHomeAdvertiseService {
    @Autowired
    private SmsHomeAdvertiseMapper advertiseMapper;

    @Override
    public int create(SmsHomeAdvertise advertise) {
        advertise.setClickCount(0);
        advertise.setOrderCount(0);
        return advertiseMapper.insert(advertise);
    }

    @Override
    public int delete(List<Long> ids) {
        // ✅ 改造：deleteByExample → delete(new LambdaQueryWrapper<>())
        return advertiseMapper.delete(new LambdaQueryWrapper<SmsHomeAdvertise>()
                .in(SmsHomeAdvertise::getId, ids));
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        SmsHomeAdvertise record = new SmsHomeAdvertise();
        record.setId(id);
        record.setStatus(status);
        return advertiseMapper.updateById(record);
    }

    @Override
    public SmsHomeAdvertise getItem(Long id) {
        // ✅ 改造：selectByPrimaryKey → selectById
        return advertiseMapper.selectById(id);
    }

    @Override
    public int update(Long id, SmsHomeAdvertise advertise) {
        // ✅ 改造：updateByPrimaryKeySelective → updateById
        advertise.setId(id);
        return advertiseMapper.updateById(advertise);
    }

    @Override
    public Page<SmsHomeAdvertise> list(String name, Integer type, String endTime, Integer pageSize, Integer pageNum) {
        // ✅ 改造：使用 MyBatis-Plus 分页
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
        // ✅ 改造：selectByExample → selectPage
        return advertiseMapper.selectPage(page, wrapper);
    }
}
