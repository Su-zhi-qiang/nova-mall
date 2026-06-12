package com.su.mall.controller;


import com.su.mall.common.api.CommonResult;
import com.su.mall.dto.OssCallbackResult;
import com.su.mall.dto.OssPolicyResult;
import com.su.mall.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Oss对象存储管理Controller
 * @author Su
 */
@RestController
@Tag(name = "OssController", description = "Oss对象存储管理")
@RequestMapping("/aliyun/oss")
@RequiredArgsConstructor
public class OssController {
    private final OssService ossService;

    @Operation(summary = "Oss上传签名生成")
    @RequestMapping(value = "/policy", method = RequestMethod.GET)
    public CommonResult<OssPolicyResult> policy() {
        OssPolicyResult result = ossService.policy();
        return CommonResult.success(result);
    }

    @Operation(summary = "Oss上传成功回调")
    @RequestMapping(value = "callback", method = RequestMethod.POST)
    public CommonResult<OssCallbackResult> callback(HttpServletRequest request) {
        OssCallbackResult ossCallbackResult = ossService.callback(request);
        return CommonResult.success(ossCallbackResult);
    }

}
