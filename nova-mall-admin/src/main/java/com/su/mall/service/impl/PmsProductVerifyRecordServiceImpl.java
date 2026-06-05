package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.PmsProductMapper;
import com.su.mall.mapper.PmsProductVerifyRecordMapper;
import com.su.mall.model.PmsProduct;
import com.su.mall.model.PmsProductVerifyRecord;
import com.su.mall.service.PmsProductVerifyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PmsProductVerifyRecordServiceImpl implements PmsProductVerifyRecordService {

    private final PmsProductMapper productMapper;
    private final PmsProductVerifyRecordMapper verifyRecordMapper;

    @Override
    public Map<String, Object> getVerifyInfo(Long productId) {
        Map<String ,Object> result = new HashMap<>();

        // 查询商品信息
        PmsProduct product = productMapper.selectById(productId);
        result.put("product", product.getId());
        result.put("productName", product.getName());
        result.put("verifyRecord", product.getVerifyStatus());

        // 查询商品最新审核记录(mp)
        LambdaQueryWrapper<PmsProductVerifyRecord> wrapper = new LambdaQueryWrapper<>();
        // 筛选出商品id等于productId的记录, 并按创建时间降序排序, 只取最新一条记录
        wrapper.eq(PmsProductVerifyRecord::getProductId, productId)
                    .orderByDesc(PmsProductVerifyRecord::getCreateTime)
                    .last("limit 1");

        PmsProductVerifyRecord record = verifyRecordMapper.selectOne(wrapper);
        if (record != null) {
            result.put("verifyRemark", record.getDetail());
            result.put("verifyTime", record.getCreateTime());
            result.put("verifyUser", record.getVerifyMan());
        }

        return result;
    }

    @Override
    @Transactional // 开启事务
    public void updateVerifyStatus(Long productId, Integer verifyStatus, String verifyRemark) {
        // 更新商品审核状态
        PmsProduct product = new PmsProduct();
        product.setId(productId);
        product.setVerifyStatus(verifyStatus);
        productMapper.updateById(product);

        // 插入审核记录
        PmsProductVerifyRecord record = new PmsProductVerifyRecord();
        record.setProductId(productId);
        record.setStatus(verifyStatus);
        record.setDetail(verifyRemark);
        record.setCreateTime(LocalDateTime.now());

        // 当前登录用户
        String currentUser = getCurrentUsername();
        // 设置审核人
        record.setVerifyMan(currentUser);
        // 插入审核记录
        verifyRecordMapper.insert(record);
    }
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        // 从SecurityContextHolder获取当前登录用户信息
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 如果principal是UserDetails类型, 则返回用户名
        if (principal instanceof UserDetails) {
            return((UserDetails) principal).getUsername();

        }
        // 如果principal不是UserDetails类型, 则返回principal的toString表示
        return principal.toString();
    }
}
