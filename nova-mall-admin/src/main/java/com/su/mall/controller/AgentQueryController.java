package com.su.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.api.CommonResult;
import com.su.mall.dto.OmsOrderDetail;
import com.su.mall.dto.OmsOrderQueryParam;
import com.su.mall.dto.PmsProductQueryParam;
import com.su.mall.mapper.OmsOrderMapper;
import com.su.mall.mapper.OmsOrderReturnApplyMapper;
import com.su.mall.mapper.UmsMemberMapper;
import com.su.mall.model.*;
import com.su.mall.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent查询专用Controller - 为AI Agent提供只读查询接口
 * 所有接口均为只读，不执行任何写操作
 */
@Tag(name = "Agent查询接口", description = "AI Agent专用只读查询API")
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentQueryController {

    private final PmsProductService productService;
    private final OmsOrderService orderService;
    private final OmsOrderMapper orderMapper;
    private final UmsMemberMapper memberMapper;
    private final UmsAdminService adminService;
    private final UmsRoleService roleService;
    private final SmsCouponService couponService;
    private final OmsOrderReturnApplyMapper returnApplyMapper;

    // ==================== 商品查询 ====================

    @Operation(summary = "Agent查询商品列表")
    @GetMapping("/products")
    public CommonResult<Map<String, Object>> queryProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        PmsProductQueryParam param = new PmsProductQueryParam();
        if (StringUtils.hasText(keyword)) param.setKeyword(keyword);
        if (brandId != null) param.setBrandId(brandId);
        if (categoryId != null) param.setProductCategoryId(categoryId);
        if (status != null) param.setPublishStatus(status);

        Page<PmsProduct> page = productService.listPage(param, pageSize, pageNum);

        List<Map<String, Object>> list = page.getRecords().stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("price", p.getPrice());
            item.put("stock", p.getStock());
            item.put("sale", p.getSale());
            item.put("publishStatus", p.getPublishStatus());
            item.put("brandId", p.getBrandId());
            item.put("productCategoryId", p.getProductCategoryId());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        result.put("totalPage", page.getPages());
        return CommonResult.success(result);
    }

    // ==================== 订单查询 ====================

    @Operation(summary = "Agent查询订单列表")
    @GetMapping("/orders")
    public CommonResult<Map<String, Object>> queryOrders(
            @RequestParam(required = false) String orderSn,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String memberUsername,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        OmsOrderQueryParam param = new OmsOrderQueryParam();
        if (StringUtils.hasText(orderSn)) param.setOrderSn(orderSn);
        if (status != null) param.setStatus(status);
        if (StringUtils.hasText(memberUsername)) param.setReceiverKeyword(memberUsername);

        Page<OmsOrder> page = orderService.list(param, pageSize, pageNum);

        List<Map<String, Object>> list = page.getRecords().stream().map(o -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", o.getId());
            item.put("orderSn", o.getOrderSn());
            item.put("memberUsername", o.getMemberUsername());
            item.put("totalAmount", o.getTotalAmount());
            item.put("payAmount", o.getPayAmount());
            item.put("status", o.getStatus());
            item.put("orderType", o.getOrderType());
            item.put("createTime", o.getCreateTime());
            item.put("payType", o.getPayType());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        result.put("totalPage", page.getPages());
        return CommonResult.success(result);
    }

    @Operation(summary = "Agent查询订单详情")
    @GetMapping("/orders/{id}")
    public CommonResult<Map<String, Object>> queryOrderDetail(@PathVariable Long id) {
        OmsOrderDetail detail = orderService.detail(id);
        if (detail == null) {
            return CommonResult.failed("订单不存在");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", detail.getId());
        result.put("orderSn", detail.getOrderSn());
        result.put("memberUsername", detail.getMemberUsername());
        result.put("status", detail.getStatus());
        result.put("totalAmount", detail.getTotalAmount());
        result.put("payAmount", detail.getPayAmount());
        result.put("createTime", detail.getCreateTime());
        result.put("receiverName", detail.getReceiverName());
        result.put("receiverPhone", detail.getReceiverPhone());
        result.put("receiverProvince", detail.getReceiverProvince());
        result.put("receiverCity", detail.getReceiverCity());
        result.put("receiverRegion", detail.getReceiverRegion());
        result.put("receiverDetailAddress", detail.getReceiverDetailAddress());

        if (detail.getOrderItemList() != null) {
            List<Map<String, Object>> items = detail.getOrderItemList().stream().map(item -> {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("productName", item.getProductName());
                itemMap.put("productPic", item.getProductPic());
                itemMap.put("productPrice", item.getProductPrice());
                itemMap.put("productQuantity", item.getProductQuantity());
                itemMap.put("realAmount", item.getRealAmount());
                return itemMap;
            }).collect(Collectors.toList());
            result.put("orderItems", items);
        }

        return CommonResult.success(result);
    }

    // ==================== 会员查询 ====================

    @Operation(summary = "Agent查询会员列表")
    @GetMapping("/members")
    public CommonResult<Map<String, Object>> queryMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<UmsMember> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(UmsMember::getUsername, keyword)
                    .or()
                    .like(UmsMember::getPhone, keyword));
        }
        wrapper.orderByDesc(UmsMember::getCreateTime);

        Page<UmsMember> page = memberMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", page.getRecords().stream().map(m -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("username", m.getUsername());
            item.put("nickname", m.getNickname());
            item.put("phone", m.getPhone());
            item.put("status", m.getStatus());
            item.put("integration", m.getIntegration());
            item.put("growth", m.getGrowth());
            item.put("createTime", m.getCreateTime());
            return item;
        }).collect(Collectors.toList()));
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        result.put("totalPage", page.getPages());
        return CommonResult.success(result);
    }

    // ==================== 统计分析 ====================

    @Operation(summary = "Agent查询销售统计")
    @GetMapping("/stats/sales")
    public CommonResult<Map<String, Object>> querySalesStats(
            @RequestParam(defaultValue = "7") Integer days) {

        // 使用订单服务查询统计数据
        OmsOrderQueryParam param = new OmsOrderQueryParam();
        Page<OmsOrder> page = orderService.list(param, 1000, 1);

        List<OmsOrder> allOrders = page.getRecords();
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();

        List<OmsOrder> recentOrders = allOrders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().after(startDate))
                .toList();

        int totalOrders = recentOrders.size();
        BigDecimal totalSales = recentOrders.stream()
                .map(OmsOrder::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgAmount = totalOrders > 0
                ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        long completedCount = recentOrders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 3).count();
        long closedCount = recentOrders.stream().filter(o -> o.getStatus() != null && o.getStatus() == 4).count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("days", days);
        result.put("orderCount", totalOrders);
        result.put("totalSales", totalSales);
        result.put("avgOrderAmount", avgAmount);
        result.put("completedCount", completedCount);
        result.put("closedCount", closedCount);
        return CommonResult.success(result);
    }

    @Operation(summary = "Agent查询热销商品")
    @GetMapping("/stats/top-products")
    public CommonResult<Map<String, Object>> queryTopProducts(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "30") Integer days) {

        PmsProductQueryParam param = new PmsProductQueryParam();
        Page<PmsProduct> page = productService.listPage(param, limit, 1);

        List<Map<String, Object>> list = page.getRecords().stream()
                .sorted(Comparator.comparingInt(PmsProduct::getSale).reversed())
                .limit(limit)
                .map(p -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", p.getId());
                    item.put("name", p.getName());
                    item.put("price", p.getPrice());
                    item.put("sale", p.getSale());
                    item.put("stock", p.getStock());
                    return item;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("limit", limit);
        result.put("days", days);
        return CommonResult.success(result);
    }

    @Operation(summary = "Agent查询库存预警")
    @GetMapping("/stats/inventory-alert")
    public CommonResult<Map<String, Object>> queryInventoryAlert(
            @RequestParam(defaultValue = "10") Integer threshold) {

        PmsProductQueryParam param = new PmsProductQueryParam();
        Page<PmsProduct> page = productService.listPage(param, 100, 1);

        List<Map<String, Object>> alertList = page.getRecords().stream()
                .filter(p -> p.getStock() != null && p.getStock() <= threshold)
                .map(p -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", p.getId());
                    item.put("name", p.getName());
                    item.put("stock", p.getStock());
                    item.put("lowStock", p.getLowStock());
                    return item;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", alertList);
        result.put("threshold", threshold);
        result.put("alertCount", alertList.size());
        return CommonResult.success(result);
    }

    @Operation(summary = "Agent查询会员统计")
    @GetMapping("/stats/members")
    public CommonResult<Map<String, Object>> queryMemberStats() {
        long totalMembers = memberMapper.selectCount(null);

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();

        LambdaQueryWrapper<UmsMember> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(UmsMember::getCreateTime, todayStart);
        long newMembersToday = memberMapper.selectCount(todayWrapper);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalMembers", totalMembers);
        result.put("newMembersToday", newMembersToday);
        return CommonResult.success(result);
    }

    @Operation(summary = "Agent查询优惠券统计")
    @GetMapping("/stats/coupons")
    public CommonResult<Map<String, Object>> queryCouponStats() {
        Page<SmsCoupon> page = couponService.list(null, null, 10, 1);

        List<Map<String, Object>> list = page.getRecords().stream().map(c -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("name", c.getName());
            item.put("amount", c.getAmount());
            item.put("publishCount", c.getPublishCount());
            item.put("useCount", c.getUseCount() != null ? c.getUseCount() : 0);
            item.put("receiveCount", c.getReceiveCount());
            // 使用率 = 使用量 / 发放量 * 100，保留1位小数
            int useCount = c.getUseCount() != null ? c.getUseCount() : 0;
            int publishCount = c.getPublishCount() != null ? c.getPublishCount() : 0;
            double usageRate = (publishCount > 0) ? (useCount * 100.0 / publishCount) : 0;
            item.put("usageRate", String.format("%.1f%%", usageRate));
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", page.getTotal());
        return CommonResult.success(result);
    }

    @Operation(summary = "Agent查询订单状态分布")
    @GetMapping("/stats/order-status")
    public CommonResult<Map<String, Object>> queryOrderStatusDistribution(
            @RequestParam(defaultValue = "7") Integer days) {

        OmsOrderQueryParam param = new OmsOrderQueryParam();
        Page<OmsOrder> page = orderService.list(param, 1000, 1);

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();

        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(0, "待付款");
        statusMap.put(1, "待发货");
        statusMap.put(2, "已发货");
        statusMap.put(3, "已完成");
        statusMap.put(4, "已关闭");
        statusMap.put(5, "无效");

        Map<Integer, Long> statusCount = page.getRecords().stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().after(startDate))
                .collect(Collectors.groupingBy(OmsOrder::getStatus, Collectors.counting()));

        List<Map<String, Object>> distribution = statusCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("status", entry.getKey());
                    item.put("statusName", statusMap.getOrDefault(entry.getKey(), "未知"));
                    item.put("count", entry.getValue());
                    return item;
                }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("days", days);
        result.put("distribution", distribution);
        return CommonResult.success(result);
    }

    // ==================== 管理员/角色 ====================

    @Operation(summary = "Agent查询管理员列表")
    @GetMapping("/admins")
    public CommonResult<Map<String, Object>> queryAdmins(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<UmsAdmin> page = adminService.list(username, pageSize, pageNum);

        List<Map<String, Object>> list = page.getRecords().stream().map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("username", a.getUsername());
            item.put("nickName", a.getNickName());
            item.put("email", a.getEmail());
            item.put("status", a.getStatus());
            item.put("createTime", a.getCreateTime());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return CommonResult.success(result);
    }

    @Operation(summary = "Agent查询角色列表")
    @GetMapping("/roles")
    public CommonResult<Map<String, Object>> queryRoles() {
        List<UmsRole> roles = roleService.list();

        List<Map<String, Object>> list = roles.stream().map(r -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("name", r.getName());
            item.put("description", r.getDescription());
            item.put("adminCount", r.getAdminCount());
            item.put("status", r.getStatus());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", list.size());
        return CommonResult.success(result);
    }

    // ==================== Dashboard统计 ====================

    @Operation(summary = "Dashboard首页统计")
    @GetMapping("/dashboard")
    public CommonResult<Map<String, Object>> getDashboard() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 获取所有订单（包括已删除的软删除订单）
        List<OmsOrder> allOrders = orderMapper.selectList(null);

        // 今日时间范围
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date todayStart = today.getTime();

        // 昨日时间范围
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        Date yesterdayStart = yesterday.getTime();

        // 今日订单统计
        List<OmsOrder> todayOrders = allOrders.stream()
                .filter(o -> o.getCreateTime() != null && !o.getCreateTime().before(todayStart))
                .collect(Collectors.toList());
        long todayOrderCount = todayOrders.size();

        // 今日销售额
        BigDecimal todaySales = todayOrders.stream()
                .map(OmsOrder::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 昨日销售额
        BigDecimal yesterdaySales = allOrders.stream()
                .filter(o -> o.getCreateTime() != null && !o.getCreateTime().before(yesterdayStart) && o.getCreateTime().before(todayStart))
                .map(OmsOrder::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 各状态订单数（所有订单，用于待处理事务统计）
        Map<Integer, Long> allStatusCount = allOrders.stream()
                .filter(o -> o.getStatus() != null)
                .collect(Collectors.groupingBy(OmsOrder::getStatus, Collectors.counting()));

        result.put("todayOrderCount", todayOrderCount);
        result.put("todaySales", todaySales);
        result.put("yesterdaySales", yesterdaySales);
        result.put("pendingPayment", allStatusCount.getOrDefault(0, 0L));  // 待付款
        result.put("pendingShipment", allStatusCount.getOrDefault(1, 0L)); // 待发货
        result.put("shipped", allStatusCount.getOrDefault(2, 0L));        // 已发货
        result.put("completed", allStatusCount.getOrDefault(3, 0L));      // 已完成

        // 商品统计
        PmsProductQueryParam productParam = new PmsProductQueryParam();
        Page<PmsProduct> productPage = productService.listPage(productParam, 10000, 1);
        List<PmsProduct> allProducts = productPage.getRecords();
        long totalProducts = productPage.getTotal();
        long onSale = allProducts.stream().filter(p -> p.getPublishStatus() != null && p.getPublishStatus() == 1).count();
        long offSale = allProducts.stream().filter(p -> p.getPublishStatus() != null && p.getPublishStatus() == 0).count();
        long lowStock = allProducts.stream().filter(p -> p.getStock() != null && p.getStock() <= 10).count();

        result.put("totalProducts", totalProducts);
        result.put("onSaleProducts", onSale);
        result.put("offSaleProducts", offSale);
        result.put("lowStockProducts", lowStock);

        // 会员统计
        long totalMembers = memberMapper.selectCount(null);

        Calendar todayMember = Calendar.getInstance();
        todayMember.set(Calendar.HOUR_OF_DAY, 0);
        todayMember.set(Calendar.MINUTE, 0);
        todayMember.set(Calendar.SECOND, 0);
        todayMember.set(Calendar.MILLISECOND, 0);
        LambdaQueryWrapper<UmsMember> todayMemberWrapper = new LambdaQueryWrapper<>();
        todayMemberWrapper.ge(UmsMember::getCreateTime, todayMember.getTime());
        long newMembersToday = memberMapper.selectCount(todayMemberWrapper);

        Calendar yesterdayMember = Calendar.getInstance();
        yesterdayMember.add(Calendar.DAY_OF_MONTH, -1);
        yesterdayMember.set(Calendar.HOUR_OF_DAY, 0);
        yesterdayMember.set(Calendar.MINUTE, 0);
        yesterdayMember.set(Calendar.SECOND, 0);
        yesterdayMember.set(Calendar.MILLISECOND, 0);
        LambdaQueryWrapper<UmsMember> yesterdayMemberWrapper = new LambdaQueryWrapper<>();
        yesterdayMemberWrapper.ge(UmsMember::getCreateTime, yesterdayMember.getTime());
        yesterdayMemberWrapper.lt(UmsMember::getCreateTime, todayMember.getTime());
        long newMembersYesterday = memberMapper.selectCount(yesterdayMemberWrapper);

        Calendar monthStart = Calendar.getInstance();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);
        monthStart.set(Calendar.MILLISECOND, 0);
        LambdaQueryWrapper<UmsMember> monthMemberWrapper = new LambdaQueryWrapper<>();
        monthMemberWrapper.ge(UmsMember::getCreateTime, monthStart.getTime());
        long newMembersMonth = memberMapper.selectCount(monthMemberWrapper);

        result.put("totalMembers", totalMembers);
        result.put("newMembersToday", newMembersToday);
        result.put("newMembersYesterday", newMembersYesterday);
        result.put("newMembersMonth", newMembersMonth);

        // 退货申请统计（待处理）
        LambdaQueryWrapper<OmsOrderReturnApply> returnApplyWrapper = new LambdaQueryWrapper<>();
        returnApplyWrapper.eq(OmsOrderReturnApply::getStatus, 0);
        long returnApply = returnApplyMapper.selectCount(returnApplyWrapper);
        result.put("returnApply", returnApply);

        // 本月销售总额（复用上面的monthStart变量）
        BigDecimal monthSales = allOrders.stream()
                .filter(o -> o.getCreateTime() != null && !o.getCreateTime().before(monthStart.getTime()))
                .map(OmsOrder::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("monthSales", monthSales);

        return CommonResult.success(result);
    }

    @Operation(summary = "Dashboard订单趋势")
    @GetMapping("/dashboard/order-trend")
    public CommonResult<List<Map<String, Object>>> getOrderTrend(
            @RequestParam(defaultValue = "7") int days) {
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = cal.getTime();

        // 获取所有订单（包括已删除的软删除订单）
        List<OmsOrder> allOrders = orderMapper.selectList(null);

        // 按日期分组统计
        Map<String, int[]> dailyStats = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (OmsOrder order : allOrders) {
            if (order.getCreateTime() != null && order.getCreateTime().after(startDate)) {
                String date = sdf.format(order.getCreateTime());
                dailyStats.computeIfAbsent(date, k -> new int[]{0, 0});
                dailyStats.get(date)[0]++;
                dailyStats.get(date)[1] += order.getPayAmount() != null ? order.getPayAmount().intValue() : 0;
            }
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : dailyStats.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", entry.getKey());
            item.put("orderCount", entry.getValue()[0]);
            item.put("orderAmount", entry.getValue()[1]);
            trend.add(item);
        }

        return CommonResult.success(trend);
    }
}
