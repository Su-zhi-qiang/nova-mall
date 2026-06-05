package com.su.mall.controller;

import com.su.mall.common.api.CommonResult;
import com.su.mall.model.CmsPrefrenceArea;
import com.su.mall.service.CmsPreferenceAreaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 商品优选管理Controller
 * @author Su
 */
@Controller
@Tag(name = "CmsPreferenceAreaController", description = "商品优选管理")
@RequestMapping("/prefrenceArea")
@RequiredArgsConstructor
public class CmsPreferenceAreaController {
    private final CmsPreferenceAreaService preferenceAreaService;

    @Operation(summary = "获取所有商品优选")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<CmsPrefrenceArea>> listAll() {
        List<CmsPrefrenceArea> prefrenceAreaList = preferenceAreaService.listAll();
        return CommonResult.success(prefrenceAreaList);
    }
}
