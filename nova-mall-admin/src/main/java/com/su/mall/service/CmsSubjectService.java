package com.su.mall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.model.CmsSubject;

import java.util.List;

/**
 * 商品专题管理Service
 * 
 */
public interface CmsSubjectService {
    /**
     * 查询所有专题
     */
    List<CmsSubject> listAll();

    /**
     * 分页查询专题
     */
    Page<CmsSubject> list(String keyword, Integer pageNum, Integer pageSize);
}
