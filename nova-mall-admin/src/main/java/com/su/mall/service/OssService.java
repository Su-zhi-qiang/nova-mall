package com.su.mall.service;

import com.su.mall.dto.OssCallbackResult;
import com.su.mall.dto.OssPolicyResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Oss对象存储管理Service
 * @author Su
 */
public interface OssService {
    /**
     * Oss上传策略生成
     */
    OssPolicyResult policy();
    /**
     * Oss上传成功回调
     */
    OssCallbackResult callback(HttpServletRequest request);
}
