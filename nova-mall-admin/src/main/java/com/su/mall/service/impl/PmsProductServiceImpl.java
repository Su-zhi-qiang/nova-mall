package com.su.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.dao.*;
import com.su.mall.dto.PmsProductParam;
import com.su.mall.dto.PmsProductQueryParam;
import com.su.mall.dto.PmsProductResult;
import com.su.mall.mapper.*;
import com.su.mall.model.*;
import com.su.mall.service.PmsProductOperateLogService;
import com.su.mall.service.PmsProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品管理Service实现类
 *
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class PmsProductServiceImpl implements PmsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PmsProductServiceImpl.class);
    private final PmsProductMapper productMapper;
    private final PmsMemberPriceDao memberPriceDao;
    private final PmsMemberPriceMapper memberPriceMapper;
    private final PmsProductLadderDao productLadderDao;
    private final PmsProductLadderMapper productLadderMapper;
    private final PmsProductFullReductionDao productFullReductionDao;
    private final PmsProductFullReductionMapper productFullReductionMapper;
    private final PmsSkuStockDao skuStockDao;
    private final PmsSkuStockMapper skuStockMapper;
    private final PmsProductAttributeValueDao productAttributeValueDao;
    private final PmsProductAttributeValueMapper productAttributeValueMapper;
    private final CmsSubjectProductRelationDao subjectProductRelationDao;
    private final CmsSubjectProductRelationMapper subjectProductRelationMapper;
    private final CmsPrefrenceAreaProductRelationDao prefrenceAreaProductRelationDao;
    private final CmsPrefrenceAreaProductRelationMapper prefrenceAreaProductRelationMapper;
    private final PmsProductDao productDao;
    private final PmsProductVertifyRecordDao productVertifyRecordDao;
    private final PmsProductOperateLogService productOperateLogService;

    @Override
    @Transactional
    public int create(PmsProductParam productParam) {
        int count;
        //创建商品
        productParam.setId(null);
        // ✅ 改造：insert 替代 insertSelective
        productMapper.insert(productParam);
        //根据促销类型设置价格：会员价格、阶梯价格、满减价格
        Long productId = productParam.getId();
        //会员价格
        relateAndInsertList(memberPriceDao, productParam.getMemberPriceList(), productId);
        //阶梯价格
        relateAndInsertList(productLadderDao, productParam.getProductLadderList(), productId);
        //满减价格
        relateAndInsertList(productFullReductionDao, productParam.getProductFullReductionList(), productId);
        //处理sku的编码
        handleSkuStockCode(productParam.getSkuStockList(),productId);
        //添加sku库存信息
        relateAndInsertList(skuStockDao, productParam.getSkuStockList(), productId);
        //添加商品参数,添加自定义商品规格
        relateAndInsertList(productAttributeValueDao, productParam.getProductAttributeValueList(), productId);
        //关联专题
        relateAndInsertList(subjectProductRelationDao, productParam.getSubjectProductRelationList(), productId);
        //关联优选
        relateAndInsertList(prefrenceAreaProductRelationDao, productParam.getPrefrenceAreaProductRelationList(), productId);
        count = 1;
        return count;
    }

    private void handleSkuStockCode(List<PmsSkuStock> skuStockList, Long productId) {
        if(CollectionUtils.isEmpty(skuStockList)) {
            return;
        }
        for(int i=0;i<skuStockList.size();i++){
            PmsSkuStock skuStock = skuStockList.get(i);
            if(StrUtil.isEmpty(skuStock.getSkuCode())){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                StringBuilder sb = new StringBuilder();
                //日期
                sb.append(sdf.format(new Date()));
                //四位商品id
                sb.append(String.format("%04d", productId));
                //三位索引id
                sb.append(String.format("%03d", i+1));
                skuStock.setSkuCode(sb.toString());
            }
        }
    }

    @Override
    public PmsProductResult getUpdateInfo(Long id) {
        return productDao.getUpdateInfo(id);
    }

    @Override
    @Transactional
    public int update(Long id, PmsProductParam productParam) {
        int count;
        // 查询旧商品信息用于记录日志
        PmsProduct oldProduct = productMapper.selectById(id);
        LOGGER.info("【商品更新】开始 - productId={}, oldProduct={}", id, oldProduct);
        
        if (oldProduct == null) {
            LOGGER.warn("【商品更新】未找到商品 - productId={}", id);
            return 0;
        }
        
        //更新商品信息
        productParam.setId(id);
        // ✅ 改造：updateById 替代 updateByPrimaryKeySelective
        int updateResult = productMapper.updateById(productParam);
        LOGGER.info("【商品更新】updateById结果: {}", updateResult);
        
        // 只有更新成功时才记录日志
        if (updateResult > 0) {
            try {
                // 重新查询更新后的数据
                PmsProduct newProduct = productMapper.selectById(id);
                LOGGER.info("【商品更新】查询到更新后的数据: {}", newProduct);
                
                if (newProduct != null) {
                    saveOperateLog(oldProduct, newProduct, "更新商品信息");
                }
            } catch (Exception e) {
                LOGGER.error("【商品更新】记录日志失败", e);
            }
        }
        
        //会员价格
        // ✅ 改造：delete + LambdaQueryWrapper 替代 deleteByExample
        memberPriceMapper.delete(new LambdaQueryWrapper<PmsMemberPrice>().eq(PmsMemberPrice::getProductId, id));
        relateAndInsertList(memberPriceDao, productParam.getMemberPriceList(), id);
        //阶梯价格
        // ✅ 改造：delete + LambdaQueryWrapper 替代 deleteByExample
        productLadderMapper.delete(new LambdaQueryWrapper<PmsProductLadder>().eq(PmsProductLadder::getProductId, id));
        relateAndInsertList(productLadderDao, productParam.getProductLadderList(), id);
        //满减价格
        // ✅ 改造：delete + LambdaQueryWrapper 替代 deleteByExample
        productFullReductionMapper.delete(new LambdaQueryWrapper<PmsProductFullReduction>().eq(PmsProductFullReduction::getProductId, id));
        relateAndInsertList(productFullReductionDao, productParam.getProductFullReductionList(), id);
        //修改sku库存信息
        handleUpdateSkuStockList(id, productParam);
        //修改商品参数,添加自定义商品规格
        // ✅ 改造：delete + LambdaQueryWrapper 替代 deleteByExample
        productAttributeValueMapper.delete(new LambdaQueryWrapper<PmsProductAttributeValue>().eq(PmsProductAttributeValue::getProductId, id));
        relateAndInsertList(productAttributeValueDao, productParam.getProductAttributeValueList(), id);
        //关联专题
        // ✅ 改造：delete + LambdaQueryWrapper 替代 deleteByExample
        subjectProductRelationMapper.delete(new LambdaQueryWrapper<CmsSubjectProductRelation>().eq(CmsSubjectProductRelation::getProductId, id));
        relateAndInsertList(subjectProductRelationDao, productParam.getSubjectProductRelationList(), id);
        //关联优选
        // ✅ 改造：delete + LambdaQueryWrapper 替代 deleteByExample
        prefrenceAreaProductRelationMapper.delete(new LambdaQueryWrapper<CmsPrefrenceAreaProductRelation>().eq(CmsPrefrenceAreaProductRelation::getProductId, id));
        relateAndInsertList(prefrenceAreaProductRelationDao, productParam.getPrefrenceAreaProductRelationList(), id);
        count = 1;
        return count;
    }

    private void handleUpdateSkuStockList(Long id, PmsProductParam productParam) {
        //当前的sku信息
        List<PmsSkuStock> currSkuList = productParam.getSkuStockList();
        //当前没有sku直接删除
        if(CollUtil.isEmpty(currSkuList)){
            // ✅ 改造：delete + LambdaQueryWrapper 替代 deleteByExample
            skuStockMapper.delete(new LambdaQueryWrapper<PmsSkuStock>().eq(PmsSkuStock::getProductId, id));
            return;
        }
        //获取初始sku信息
        // ✅ 改造：selectList + LambdaQueryWrapper 替代 selectByExample
        List<PmsSkuStock> oriStuList = skuStockMapper.selectList(
            new LambdaQueryWrapper<PmsSkuStock>().eq(PmsSkuStock::getProductId, id)
        );
        //获取新增sku信息
        List<PmsSkuStock> insertSkuList = currSkuList.stream().filter(item->item.getId()==null).collect(Collectors.toList());
        //获取需要更新的sku信息
        List<PmsSkuStock> updateSkuList = currSkuList.stream().filter(item->item.getId()!=null).collect(Collectors.toList());
        List<Long> updateSkuIds = updateSkuList.stream().map(PmsSkuStock::getId).toList();
        //获取需要删除的sku信息
        List<PmsSkuStock> removeSkuList = oriStuList.stream().filter(item-> !updateSkuIds.contains(item.getId())).collect(Collectors.toList());
        handleSkuStockCode(insertSkuList,id);
        handleSkuStockCode(updateSkuList,id);
        //新增sku
        if(CollUtil.isNotEmpty(insertSkuList)){
            relateAndInsertList(skuStockDao, insertSkuList, id);
        }
        //删除sku
        if(CollUtil.isNotEmpty(removeSkuList)){
            List<Long> removeSkuIds = removeSkuList.stream().map(PmsSkuStock::getId).collect(Collectors.toList());
            // ✅ 改造：deleteByIds 替代 deleteByExample
            skuStockMapper.deleteByIds(removeSkuIds);
        }
        //修改sku
        if(CollUtil.isNotEmpty(updateSkuList)){
            for (PmsSkuStock pmsSkuStock : updateSkuList) {
                // ✅ 改造：updateById 替代 updateByPrimaryKeySelective
                skuStockMapper.updateById(pmsSkuStock);
            }
        }

    }



    @Override
    public Page<PmsProduct> listPage(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum) {
        LOGGER.info("查询商品列表（MyBatis-Plus） - pageNum: {}, pageSize: {}, 查询条件: {}", pageNum, pageSize, productQueryParam);
        
        // 先统计总数，用于调试
        LambdaQueryWrapper<PmsProduct> countWrapper = new LambdaQueryWrapper<>();
        // 查询未删除的商品，同时处理 deleteStatus 为 null 的情况
        countWrapper.and(w -> w.eq(PmsProduct::getDeleteStatus, 0).or().isNull(PmsProduct::getDeleteStatus));
        if (productQueryParam.getPublishStatus() != null) {
            countWrapper.eq(PmsProduct::getPublishStatus, productQueryParam.getPublishStatus());
        }
        if (productQueryParam.getVerifyStatus() != null) {
            countWrapper.eq(PmsProduct::getVerifyStatus, productQueryParam.getVerifyStatus());
        }
        if (!StrUtil.isEmpty(productQueryParam.getKeyword())) {
            countWrapper.like(PmsProduct::getName, productQueryParam.getKeyword());
        }
        if (!StrUtil.isEmpty(productQueryParam.getProductSn())) {
            countWrapper.eq(PmsProduct::getProductSn, productQueryParam.getProductSn());
        }
        if (productQueryParam.getBrandId() != null) {
            countWrapper.eq(PmsProduct::getBrandId, productQueryParam.getBrandId());
        }
        if (productQueryParam.getProductCategoryId() != null) {
            countWrapper.eq(PmsProduct::getProductCategoryId, productQueryParam.getProductCategoryId());
        }
        long totalCount = productMapper.selectCount(countWrapper);
        LOGGER.info("符合条件的商品总数: {}", totalCount);

        // 构建查询条件
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        // 查询未删除的商品，同时处理 deleteStatus 为 null 的情况
        wrapper.and(w -> w.eq(PmsProduct::getDeleteStatus, 0).or().isNull(PmsProduct::getDeleteStatus));
        if (productQueryParam.getPublishStatus() != null) {
            wrapper.eq(PmsProduct::getPublishStatus, productQueryParam.getPublishStatus());
        }
        if (productQueryParam.getVerifyStatus() != null) {
            wrapper.eq(PmsProduct::getVerifyStatus, productQueryParam.getVerifyStatus());
        }
        if (!StrUtil.isEmpty(productQueryParam.getKeyword())) {
            wrapper.like(PmsProduct::getName, productQueryParam.getKeyword());
        }
        if (!StrUtil.isEmpty(productQueryParam.getProductSn())) {
            wrapper.eq(PmsProduct::getProductSn, productQueryParam.getProductSn());
        }
        if (productQueryParam.getBrandId() != null) {
            wrapper.eq(PmsProduct::getBrandId, productQueryParam.getBrandId());
        }
        if (productQueryParam.getProductCategoryId() != null) {
            wrapper.eq(PmsProduct::getProductCategoryId, productQueryParam.getProductCategoryId());
        }
        
        // 创建分页对象并执行查询
        Page<PmsProduct> page = new Page<>(pageNum, pageSize);
        Page<PmsProduct> result = productMapper.selectPage(page, wrapper);
        LOGGER.info("本次查询返回商品数量: {}, 总数: {}", result.getRecords().size(), result.getTotal());
        
        return result;
    }

    @Override
    @Transactional
    public int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail) {
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct product = new PmsProduct();
        product.setVerifyStatus(verifyStatus);
        int count = productMapper.update(
            product,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
        
        // 记录操作日志
        if (count > 0 && ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                try {
                    String statusDesc = verifyStatus == 1 ? "审核通过" : (verifyStatus == 0 ? "审核未通过" : "审核状态变更");
                    saveSimpleOperateLog(id, statusDesc);
                } catch (Exception e) {
                    LOGGER.error("【商品审核】记录日志失败 - productId={}", id, e);
                }
            }
        }
        
        //修改完审核状态后插入审核记录
        if (ids != null && !ids.isEmpty()) {
            List<PmsProductVerifyRecord> list = new ArrayList<>();
            for (Long id : ids) {
                PmsProductVerifyRecord record = new PmsProductVerifyRecord();
                record.setProductId(id);
                record.setCreateTime(LocalDateTime.now());
                record.setDetail(detail);
                record.setStatus(verifyStatus);
                record.setVerifyMan("test");
                list.add(record);
            }
            productVertifyRecordDao.insertList(list);
        }
        return count;
    }

    @Override
    public int updatePublishStatus(List<Long> ids, Integer publishStatus) {
        LOGGER.info("【商品上架/下架】开始 - ids={}, publishStatus={}", ids, publishStatus);
        
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct record = new PmsProduct();
        record.setPublishStatus(publishStatus);
        int count = productMapper.update(
            record,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
        
        LOGGER.info("【商品上架/下架】更新结果: count={}", count);
        
        // 记录操作日志
        if (count > 0 && ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                try {
                    String statusDesc = publishStatus == 1 ? "商品上架" : "商品下架";
                    saveSimpleOperateLog(id, statusDesc);
                } catch (Exception e) {
                    LOGGER.error("【商品上架/下架】记录日志失败 - productId={}", id, e);
                }
            }
        }
        
        return count;
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        LOGGER.info("【商品推荐】开始 - ids={}, recommendStatus={}", ids, recommendStatus);
        
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct record = new PmsProduct();
        record.setRecommandStatus(recommendStatus);
        int count = productMapper.update(
            record,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
        
        LOGGER.info("【商品推荐】更新结果: count={}", count);
        
        // 记录操作日志
        if (count > 0 && ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                try {
                    String statusDesc = recommendStatus == 1 ? "设为推荐" : "取消推荐";
                    saveSimpleOperateLog(id, statusDesc);
                } catch (Exception e) {
                    LOGGER.error("【商品推荐】记录日志失败 - productId={}", id, e);
                }
            }
        }
        
        return count;
    }

    @Override
    public int updateNewStatus(List<Long> ids, Integer newStatus) {
        LOGGER.info("【商品新品】开始 - ids={}, newStatus={}", ids, newStatus);
        
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct record = new PmsProduct();
        record.setNewStatus(newStatus);
        int count = productMapper.update(
            record,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
        
        LOGGER.info("【商品新品】更新结果: count={}", count);
        
        // 记录操作日志
        if (count > 0 && ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                try {
                    String statusDesc = newStatus == 1 ? "设为新品" : "取消新品";
                    saveSimpleOperateLog(id, statusDesc);
                } catch (Exception e) {
                    LOGGER.error("【商品新品】记录日志失败 - productId={}", id, e);
                }
            }
        }
        
        return count;
    }

    @Override
    @Transactional
    public int updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
        LOGGER.info("【商品删除】开始 - ids={}, deleteStatus={}", ids, deleteStatus);
        
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct record = new PmsProduct();
        record.setDeleteStatus(deleteStatus);
        int count = productMapper.update(
            record,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
        
        LOGGER.info("【商品删除】更新结果: count={}", count);
        
        // 记录操作日志
        if (count > 0 && ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                try {
                    String statusDesc = deleteStatus == 1 ? "删除商品" : "恢复商品";
                    saveSimpleOperateLog(id, statusDesc);
                } catch (Exception e) {
                    LOGGER.error("【商品删除】记录日志失败 - productId={}", id, e);
                }
            }
        }
        
        return count;
    }

    @Override
    public List<PmsProduct> list(String keyword) {
        // ✅ 改造：LambdaQueryWrapper 替代 Example（使用链式调用实现 OR 查询）
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProduct::getDeleteStatus, 0)
            .and(!StrUtil.isEmpty(keyword), 
                w -> w.like(PmsProduct::getName, keyword)
                    .or()
                    .like(PmsProduct::getProductSn, keyword)
            );
        return productMapper.selectList(wrapper);
    }

    /**
     * 建立和插入关系表操作
     *
     * @param dao       可以操作的dao
     * @param dataList  要插入的数据
     * @param productId 建立关系的id
     */
    private void relateAndInsertList(Object dao, List<?> dataList, Long productId) {
        try {
            if (CollectionUtils.isEmpty(dataList)) {
                return;
            }
            for (Object item : dataList) {
                Method setId = item.getClass().getMethod("setId", Long.class);
                setId.invoke(item, (Long) null);
                Method setProductId = item.getClass().getMethod("setProductId", Long.class);
                setProductId.invoke(item, productId);
            }
            Method insertList = dao.getClass().getMethod("insertList", List.class);
            insertList.invoke(dao, dataList);
        } catch (Exception e) {
            LOGGER.warn("创建产品出错:{}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 保存商品操作日志
     *
     * @param oldProduct 旧商品信息
     * @param newProduct 新商品信息
     * @param operateDesc 操作描述
     */
    private void saveOperateLog(PmsProduct oldProduct, PmsProduct newProduct, String operateDesc) {
        if (oldProduct == null || newProduct == null) {
            LOGGER.warn("【商品日志】保存失败 - oldProduct或newProduct为空");
            return;
        }

        try {
            PmsProductOperateLog log = new PmsProductOperateLog();
            log.setProductId(oldProduct.getId());
            log.setPriceOld(oldProduct.getPrice());
            log.setPriceNew(newProduct.getPrice());
            log.setSalePriceOld(oldProduct.getPromotionPrice());
            log.setSalePriceNew(newProduct.getPromotionPrice());
            log.setGiftPointOld(oldProduct.getGiftPoint());
            log.setGiftPointNew(newProduct.getGiftPoint());
            log.setUsePointLimitOld(oldProduct.getUsePointLimit());
            log.setUsePointLimitNew(newProduct.getUsePointLimit());

            // 审核信息
            log.setVerifyStatusOld(oldProduct.getVerifyStatus());
            log.setVerifyStatusNew(newProduct.getVerifyStatus());

            log.setOperateMan("admin");
            log.setCreateTime(new Date());

            LOGGER.info("【商品日志】准备保存 - productId={}, operateDesc={}", oldProduct.getId(), operateDesc);
            
            productOperateLogService.saveLog(log);
            LOGGER.info("【商品日志】保存成功 - productId={}", oldProduct.getId());
        } catch (Exception e) {
            LOGGER.error("【商品日志】保存异常 - productId={}", oldProduct != null ? oldProduct.getId() : null, e);
            throw e;
        }
    }

    /**
     * 保存简单的操作日志（只记录操作，无字段变更）
     */
    private void saveSimpleOperateLog(Long productId, String operateDesc) {
        if (productId == null) {
            LOGGER.warn("【商品日志】productId为空，跳过保存");
            return;
        }

        try {
            PmsProduct product = productMapper.selectById(productId);
            if (product == null) {
                LOGGER.warn("【商品日志】未找到商品 - productId={}", productId);
                return;
            }
            // 复用 saveOperateLog，oldProduct 和 newProduct 是同一个（无字段变更）
            saveOperateLog(product, product, operateDesc);
        } catch (Exception e) {
            LOGGER.error("【商品日志】保存简单日志异常 - productId={}", productId, e);
        }
    }

}