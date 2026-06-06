package com.su.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.su.mall.mapper.PmsProductMapper;
import com.su.mall.mapper.PmsProductVerifyRecordMapper;
import com.su.mall.model.PmsProduct;
import com.su.mall.model.PmsProductOperateLog;
import com.su.mall.model.PmsProductVerifyRecord;
import com.su.mall.service.PmsProductOperateLogService;
import com.su.mall.service.PmsProductVerifyRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PmsProductVerifyRecordServiceImpl implements PmsProductVerifyRecordService {

    private final PmsProductMapper productMapper;
    private final PmsProductVerifyRecordMapper verifyRecordMapper;
    private final PmsProductOperateLogService productOperateLogService;

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
        log.info("【商品审核】开始 - productId={}, verifyStatus={}, verifyRemark={}", productId, verifyStatus, verifyRemark);
        
        // 先查询旧的商品信息，用于记录日志
        PmsProduct oldProduct = productMapper.selectById(productId);
        if (oldProduct == null) {
            log.warn("【商品审核】未找到商品 - productId={}", productId);
            throw new RuntimeException("商品不存在");
        }
        
        // 查询旧的审核记录，获取旧的备注
        String oldVerifyRemark = null;
        LambdaQueryWrapper<PmsProductVerifyRecord> oldRecordWrapper = new LambdaQueryWrapper<>();
        oldRecordWrapper.eq(PmsProductVerifyRecord::getProductId, productId)
                .orderByDesc(PmsProductVerifyRecord::getCreateTime)
                .last("limit 1");
        PmsProductVerifyRecord oldRecord = verifyRecordMapper.selectOne(oldRecordWrapper);
        if (oldRecord != null) {
            oldVerifyRemark = oldRecord.getDetail();
        }
        
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
        
        // 记录操作日志
        saveOperateLog(oldProduct, verifyStatus, oldVerifyRemark, verifyRemark, currentUser);
        
        log.info("【商品审核】完成 - productId={}", productId);
    }
    
    /**
     * 保存商品操作日志
     *
     * @param oldProduct 旧商品信息
     * @param newVerifyStatus 新审核状态
     * @param oldVerifyRemark 旧审核备注
     * @param newVerifyRemark 新审核备注
     * @param operateMan 操作人
     */
    private void saveOperateLog(PmsProduct oldProduct, Integer newVerifyStatus, 
                             String oldVerifyRemark, String newVerifyRemark, String operateMan) {
        try {
            PmsProductOperateLog operateLog = new PmsProductOperateLog();
            operateLog.setProductId(oldProduct.getId());
            operateLog.setPriceOld(oldProduct.getPrice());
            operateLog.setPriceNew(oldProduct.getPrice());
            operateLog.setSalePriceOld(oldProduct.getPromotionPrice());
            operateLog.setSalePriceNew(oldProduct.getPromotionPrice());
            operateLog.setGiftPointOld(oldProduct.getGiftPoint());
            operateLog.setGiftPointNew(oldProduct.getGiftPoint());
            operateLog.setUsePointLimitOld(oldProduct.getUsePointLimit());
            operateLog.setUsePointLimitNew(oldProduct.getUsePointLimit());
            
            // 审核信息
            operateLog.setVerifyStatusOld(oldProduct.getVerifyStatus());
            operateLog.setVerifyStatusNew(newVerifyStatus);
            operateLog.setVerifyRemarkOld(oldVerifyRemark);
            operateLog.setVerifyRemarkNew(newVerifyRemark);
            
            operateLog.setOperateMan(operateMan);
            operateLog.setCreateTime(new Date());

            log.info("【商品日志】准备保存审核日志 - productId={}, verifyStatusOld={}, verifyStatusNew={}, verifyRemarkOld={}, verifyRemarkNew={}", 
                oldProduct.getId(), oldProduct.getVerifyStatus(), newVerifyStatus, oldVerifyRemark, newVerifyRemark);
            
            productOperateLogService.saveLog(operateLog);
            log.info("【商品日志】审核日志保存成功 - productId={}", oldProduct.getId());
        } catch (Exception e) {
            log.error("【商品日志】保存审核日志异常 - productId={}", oldProduct != null ? oldProduct.getId() : null, e);
        }
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
