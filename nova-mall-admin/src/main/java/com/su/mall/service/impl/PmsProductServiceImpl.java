package com.su.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.su.mall.dao.*;
import com.su.mall.dto.PmsProductParam;
import com.su.mall.dto.PmsProductQueryParam;
import com.su.mall.dto.PmsProductResult;
import com.su.mall.mapper.*;
import com.su.mall.model.*;
import com.su.mall.service.PmsProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
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
public class PmsProductServiceImpl implements PmsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PmsProductServiceImpl.class);
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsMemberPriceDao memberPriceDao;
    @Autowired
    private PmsMemberPriceMapper memberPriceMapper;
    @Autowired
    private PmsProductLadderDao productLadderDao;
    @Autowired
    private PmsProductLadderMapper productLadderMapper;
    @Autowired
    private PmsProductFullReductionDao productFullReductionDao;
    @Autowired
    private PmsProductFullReductionMapper productFullReductionMapper;
    @Autowired
    private PmsSkuStockDao skuStockDao;
    @Autowired
    private PmsSkuStockMapper skuStockMapper;
    @Autowired
    private PmsProductAttributeValueDao productAttributeValueDao;
    @Autowired
    private PmsProductAttributeValueMapper productAttributeValueMapper;
    @Autowired
    private CmsSubjectProductRelationDao subjectProductRelationDao;
    @Autowired
    private CmsSubjectProductRelationMapper subjectProductRelationMapper;
    @Autowired
    private CmsPrefrenceAreaProductRelationDao prefrenceAreaProductRelationDao;
    @Autowired
    private CmsPrefrenceAreaProductRelationMapper prefrenceAreaProductRelationMapper;
    @Autowired
    private PmsProductDao productDao;
    @Autowired
    private PmsProductVertifyRecordDao productVertifyRecordDao;

    @Override
    public int create(PmsProductParam productParam) {
        int count;
        //创建商品
        PmsProduct product = productParam;
        product.setId(null);
        // ✅ 改造：insert 替代 insertSelective
        productMapper.insert(product);
        //根据促销类型设置价格：会员价格、阶梯价格、满减价格
        Long productId = product.getId();
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
        if(CollectionUtils.isEmpty(skuStockList))return;
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
    public int update(Long id, PmsProductParam productParam) {
        int count;
        //更新商品信息
        PmsProduct product = productParam;
        product.setId(id);
        // ✅ 改造：updateById 替代 updateByPrimaryKeySelective
        productMapper.updateById(product);
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
        List<Long> updateSkuIds = updateSkuList.stream().map(PmsSkuStock::getId).collect(Collectors.toList());
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
    public List<PmsProduct> list(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        // ✅ 改造：LambdaQueryWrapper 替代 Example
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProduct::getDeleteStatus, 0);
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
        return productMapper.selectList(wrapper);
    }

    @Override
    public int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail) {
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct product = new PmsProduct();
        product.setVerifyStatus(verifyStatus);
        int count = productMapper.update(
            product,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
        //修改完审核状态后插入审核记录
        List<PmsProductVertifyRecord> list = new ArrayList<>();
        for (Long id : ids) {
            PmsProductVertifyRecord record = new PmsProductVertifyRecord();
            record.setProductId(id);
            record.setCreateTime(new Date());
            record.setDetail(detail);
            record.setStatus(verifyStatus);
            record.setVertifyMan("test");
            list.add(record);
        }
        productVertifyRecordDao.insertList(list);
        return count;
    }

    @Override
    public int updatePublishStatus(List<Long> ids, Integer publishStatus) {
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct record = new PmsProduct();
        record.setPublishStatus(publishStatus);
        return productMapper.update(
            record,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct record = new PmsProduct();
        record.setRecommandStatus(recommendStatus);
        return productMapper.update(
            record,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
    }

    @Override
    public int updateNewStatus(List<Long> ids, Integer newStatus) {
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct record = new PmsProduct();
        record.setNewStatus(newStatus);
        return productMapper.update(
            record,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
    }

    @Override
    public int updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
        // ✅ 改造：update + LambdaUpdateWrapper 替代 updateByExampleSelective
        PmsProduct record = new PmsProduct();
        record.setDeleteStatus(deleteStatus);
        return productMapper.update(
            record,
            new LambdaUpdateWrapper<PmsProduct>().in(PmsProduct::getId, ids)
        );
    }

    @Override
    public List<PmsProduct> list(String keyword) {
        // ✅ 改造：LambdaQueryWrapper 替代 Example（使用链式调用实现 OR 查询）
        LambdaQueryWrapper<PmsProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsProduct::getDeleteStatus, 0)
            .and(!StrUtil.isEmpty(keyword), 
                w -> w.like(PmsProduct::getName, keyword)
                    .or()
                    .eq(PmsProduct::getDeleteStatus, 0)
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
    private void relateAndInsertList(Object dao, List dataList, Long productId) {
        try {
            if (CollectionUtils.isEmpty(dataList)) return;
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

}
