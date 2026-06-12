package com.su.mall.controller;

import com.su.mall.common.api.CommonResult;
import com.su.mall.dto.SmsFlashPromotionSessionDetail;
import com.su.mall.model.SmsFlashPromotionSession;
import com.su.mall.service.SmsFlashPromotionSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 限时购场次管理Controller
 * @author Su
 */
@RestController
@Tag(name = "SmsFlashPromotionSessionController", description = "限时购场次管理")
@RequestMapping("/flashSession")
@RequiredArgsConstructor
public class SmsFlashPromotionSessionController {
    private final SmsFlashPromotionSessionService flashPromotionSessionService;

    @Operation(summary = "添加场次")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult<Integer> create(@RequestBody SmsFlashPromotionSession promotionSession) {
        int count = flashPromotionSessionService.create(promotionSession);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改场次")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody SmsFlashPromotionSession promotionSession) {
        int count = flashPromotionSessionService.update(id, promotionSession);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改启用状态")
    @RequestMapping(value = "/update/status/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> updateStatus(@PathVariable Long id, Integer status) {
        int count = flashPromotionSessionService.updateStatus(id, status);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "删除场次")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> delete(@PathVariable Long id) {
        int count = flashPromotionSessionService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取场次详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<SmsFlashPromotionSession> getItem(@PathVariable Long id) {
        SmsFlashPromotionSession promotionSession = flashPromotionSessionService.getItem(id);
        return CommonResult.success(promotionSession);
    }

    @Operation(summary = "获取全部场次")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<SmsFlashPromotionSession>> list() {
        List<SmsFlashPromotionSession> promotionSessionList = flashPromotionSessionService.list();
        return CommonResult.success(promotionSessionList);
    }

    @Operation(summary = "获取全部可选场次及其数量")
    @RequestMapping(value = "/selectList", method = RequestMethod.GET)
    public CommonResult<List<SmsFlashPromotionSessionDetail>> selectList(Long flashPromotionId) {
        List<SmsFlashPromotionSessionDetail> promotionSessionList = flashPromotionSessionService.selectList(flashPromotionId);
        return CommonResult.success(promotionSessionList);
    }

    @Operation(summary = "重置场次秒杀库存（还原为原始库存，并清零已抢购数）")
    @RequestMapping(value = "/resetStock", method = RequestMethod.POST)
    public CommonResult<Integer> resetFlashStock(Long flashPromotionId, Long flashPromotionSessionId) {
        int count = flashPromotionSessionService.resetFlashStock(flashPromotionId, flashPromotionSessionId);
        if (count >= 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}