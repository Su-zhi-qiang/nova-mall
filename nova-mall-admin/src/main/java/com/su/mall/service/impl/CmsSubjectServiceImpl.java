package com.su.mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.mapper.CmsSubjectMapper;
import com.su.mall.model.CmsSubject;
import com.su.mall.service.CmsSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品专题管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class CmsSubjectServiceImpl implements CmsSubjectService {
    private final CmsSubjectMapper subjectMapper;

    @Override
    public List<CmsSubject> listAll() {
        // ✅ 改造：selectByExample → selectList + LambdaQueryWrapper
        return subjectMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public Page<CmsSubject> list(String keyword, Integer pageNum, Integer pageSize) {
        Page<CmsSubject> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList + LambdaQueryWrapper
        LambdaQueryWrapper<CmsSubject> wrapper = new LambdaQueryWrapper<>();
        if (!StrUtil.isEmpty(keyword)) {
            wrapper.like(CmsSubject::getTitle, keyword);
        }
        return subjectMapper.selectPage(page, wrapper);
    }
}
