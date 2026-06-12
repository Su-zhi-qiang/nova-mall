package com.su.mall.portal.controller;

import com.su.mall.common.api.CommonPage;
import com.su.mall.common.api.CommonResult;
import com.su.mall.portal.domain.ConfirmOrderResult;
import com.su.mall.portal.domain.OmsOrderDetail;
import com.su.mall.portal.domain.OrderParam;
import com.su.mall.portal.service.OmsPortalOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理Controller
 * @author Su
 */
@RestController
@Tag(name = "OmsPortalOrderController", description = "订单管理")
@RequestMapping("/order")
@RequiredArgsConstructor
public class OmsPortalOrderController {
    private final OmsPortalOrderService portalOrderService;

    @Operation(summary = "根据购物车信息生成确认单")
    @RequestMapping(value = "/generateConfirmOrder", method = RequestMethod.POST)
    public CommonResult<ConfirmOrderResult> generateConfirmOrder(@RequestBody List<Long> cartIds) {
        ConfirmOrderResult confirmOrderResult = portalOrderService.generateConfirmOrder(cartIds);
        return CommonResult.success(confirmOrderResult);
    }

    @Operation(summary = "根据购物车信息生成订单")
    @RequestMapping(value = "/generateOrder", method = RequestMethod.POST)
    public CommonResult<Map<String, Object>> generateOrder(@RequestBody OrderParam orderParam) {
        Map<String, Object> result = portalOrderService.generateOrder(orderParam);
        return CommonResult.success(result, "下单成功");
    }

    @Operation(summary = "用户支付成功的回调")
    @RequestMapping(value = "/paySuccess", method = RequestMethod.POST)
    public CommonResult<Integer> paySuccess(@RequestParam Long orderId,@RequestParam Integer payType) {
        Integer count = portalOrderService.paySuccess(orderId,payType);
        return CommonResult.success(count, "支付成功");

    }

    @Operation(summary = "自动取消超时订单")
    @RequestMapping(value = "/cancelTimeOutOrder", method = RequestMethod.POST)
    public CommonResult<Void> cancelTimeOutOrder() {
        portalOrderService.cancelTimeOutOrder();
        return CommonResult.success(null);
    }

    @Operation(summary = "取消单个超时订单")
    @RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
    public CommonResult<Void> cancelOrder(Long orderId) {
        portalOrderService.sendDelayMessageCancelOrder(orderId);
        return CommonResult.success(null);
    }

    @Operation(summary = "按状态分页获取用户订单列表")
    @Parameter(name = "status", description = "订单状态：-1->全部；0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭",
            in = ParameterIn.QUERY, schema = @Schema(type = "integer",defaultValue = "-1",allowableValues = {"-1","0","1","2","3","4"}))
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<OmsOrderDetail>> list(@RequestParam Integer status,
                                                   @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                   @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        CommonPage<OmsOrderDetail> orderPage = portalOrderService.list(status,pageNum,pageSize);
        return CommonResult.success(orderPage);
    }

    @Operation(summary = "根据ID获取订单详情")
    @RequestMapping(value = "/detail/{orderId}", method = RequestMethod.GET)
    public CommonResult<OmsOrderDetail> detail(@PathVariable Long orderId) {
        OmsOrderDetail orderDetail = portalOrderService.detail(orderId);
        return CommonResult.success(orderDetail);
    }

    @Operation(summary = "用户取消订单")
    @RequestMapping(value = "/cancelUserOrder", method = RequestMethod.POST)
    public CommonResult<Void> cancelUserOrder(Long orderId) {
        portalOrderService.cancelOrder(orderId);
        return CommonResult.success(null);
    }

    @Operation(summary = "用户确认收货")
    @RequestMapping(value = "/confirmReceiveOrder", method = RequestMethod.POST)
    public CommonResult<Void> confirmReceiveOrder(Long orderId) {
        portalOrderService.confirmReceiveOrder(orderId);
        return CommonResult.success(null);
    }

    @Operation(summary = "用户删除订单")
    @RequestMapping(value = "/deleteOrder", method = RequestMethod.POST)
    public CommonResult<Void> deleteOrder(Long orderId) {
        portalOrderService.deleteOrder(orderId);
        return CommonResult.success(null);
    }
}
