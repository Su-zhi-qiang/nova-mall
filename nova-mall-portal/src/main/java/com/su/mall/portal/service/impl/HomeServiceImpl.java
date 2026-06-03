package com.su.mall.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.su.mall.mapper.*;
import com.su.mall.model.*;
import com.su.mall.portal.dao.HomeDao;
import com.su.mall.portal.domain.FlashPromotionProduct;
import com.su.mall.portal.domain.HomeContentResult;
import com.su.mall.portal.domain.HomeFlashPromotion;
import com.su.mall.portal.service.HomeService;
import com.su.mall.portal.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 首页内容管理Service实现类
 * @author Su
 */
@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private SmsHomeAdvertiseMapper advertiseMapper;
    @Autowired
    private HomeDao homeDao;
    @Autowired
    private SmsFlashPromotionMapper flashPromotionMapper;
    @Autowired
    private SmsFlashPromotionSessionMapper promotionSessionMapper;
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsProductCategoryMapper productCategoryMapper;
    @Autowired
    private CmsSubjectMapper subjectMapper;

    @Override
    public HomeContentResult content() {
        HomeContentResult result = new HomeContentResult();
        //获取首页广告
        result.setAdvertiseList(getHomeAdvertiseList());
        //获取推荐品牌
        result.setBrandList(homeDao.getRecommendBrandList(0,6));
        //获取秒杀信息
        result.setHomeFlashPromotion(getHomeFlashPromotion());
        //获取新品推荐
        result.setNewProductList(homeDao.getNewProductList(0,4));
        //获取人气推荐
        result.setHotProductList(homeDao.getHotProductList(0,4));
        //获取推荐专题
        result.setSubjectList(homeDao.getRecommendSubjectList(0,4));
        return result;
    }

    @Override
    public List<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum) {
        // TODO: 2019/1/29 暂时默认推荐所有商品
        PageHelper.startPage(pageNum,pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProduct>())
        return productMapper.selectList(
                new LambdaQueryWrapper<PmsProduct>()
                        .eq(PmsProduct::getDeleteStatus, 0)
                        .eq(PmsProduct::getPublishStatus, 1));
    }

    @Override
    public List<PmsProductCategory> getProductCateList(Long parentId) {
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProductCategory>())
        return productCategoryMapper.selectList(
                new LambdaQueryWrapper<PmsProductCategory>()
                        .eq(PmsProductCategory::getShowStatus, 1)
                        .eq(PmsProductCategory::getParentId, parentId)
                        .orderByDesc(PmsProductCategory::getSort));
    }

    @Override
    public List<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<CmsSubject>())
        return subjectMapper.selectList(
                new LambdaQueryWrapper<CmsSubject>()
                        .eq(CmsSubject::getShowStatus, 1)
                        .eq(cateId != null, CmsSubject::getCategoryId, cateId));
    }

    @Override
    public List<PmsProduct> hotProductList(Integer pageNum, Integer pageSize) {
        int offset = pageSize * (pageNum - 1);
        return homeDao.getHotProductList(offset, pageSize);
    }

    @Override
    public List<PmsProduct> newProductList(Integer pageNum, Integer pageSize) {
        int offset = pageSize * (pageNum - 1);
        return homeDao.getNewProductList(offset, pageSize);
    }

    private HomeFlashPromotion getHomeFlashPromotion() {
        HomeFlashPromotion homeFlashPromotion = new HomeFlashPromotion();
        //获取当前秒杀活动
        Date now = new Date();
        SmsFlashPromotion flashPromotion = getFlashPromotion(now);
        if (flashPromotion != null) {
            //获取当前秒杀场次
            SmsFlashPromotionSession flashPromotionSession = getFlashPromotionSession(now);
            if (flashPromotionSession != null) {
                homeFlashPromotion.setStartTime(flashPromotionSession.getStartTime());
                homeFlashPromotion.setEndTime(flashPromotionSession.getEndTime());
                //获取下一个秒杀场次
                SmsFlashPromotionSession nextSession = getNextFlashPromotionSession(homeFlashPromotion.getStartTime());
                if(nextSession!=null){
                    homeFlashPromotion.setNextStartTime(nextSession.getStartTime());
                    homeFlashPromotion.setNextEndTime(nextSession.getEndTime());
                }
                //获取秒杀商品
                List<FlashPromotionProduct> flashProductList = homeDao.getFlashProductList(flashPromotion.getId(), flashPromotionSession.getId());
                homeFlashPromotion.setProductList(flashProductList);
            }
        }
        return homeFlashPromotion;
    }

    //获取下一个场次信息
    private SmsFlashPromotionSession getNextFlashPromotionSession(Date date) {
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<SmsFlashPromotionSession>())
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotionSession>()
                        .gt(SmsFlashPromotionSession::getStartTime, date)
                        .orderByAsc(SmsFlashPromotionSession::getStartTime));
        if (!CollectionUtils.isEmpty(promotionSessionList)) {
            return promotionSessionList.get(0);
        }
        return null;
    }

    private List<SmsHomeAdvertise> getHomeAdvertiseList() {
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<SmsHomeAdvertise>())
        return advertiseMapper.selectList(
                new LambdaQueryWrapper<SmsHomeAdvertise>()
                        .eq(SmsHomeAdvertise::getType, 1)
                        .eq(SmsHomeAdvertise::getStatus, 1)
                        .orderByDesc(SmsHomeAdvertise::getSort));
    }

    //根据时间获取秒杀活动
    private SmsFlashPromotion getFlashPromotion(Date date) {
        Date currDate = DateUtil.getDate(date);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<SmsFlashPromotion>())
        List<SmsFlashPromotion> flashPromotionList = flashPromotionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotion>()
                        .eq(SmsFlashPromotion::getStatus, 1)
                        .le(SmsFlashPromotion::getStartDate, currDate)
                        .ge(SmsFlashPromotion::getEndDate, currDate));
        if (!CollectionUtils.isEmpty(flashPromotionList)) {
            return flashPromotionList.get(0);
        }
        return null;
    }

    //根据时间获取秒杀场次
    private SmsFlashPromotionSession getFlashPromotionSession(Date date) {
        Date currTime = DateUtil.getTime(date);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<SmsFlashPromotionSession>())
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotionSession>()
                        .le(SmsFlashPromotionSession::getStartTime, currTime)
                        .ge(SmsFlashPromotionSession::getEndTime, currTime));
        if (!CollectionUtils.isEmpty(promotionSessionList)) {
            return promotionSessionList.get(0);
        }
        return null;
    }
}
