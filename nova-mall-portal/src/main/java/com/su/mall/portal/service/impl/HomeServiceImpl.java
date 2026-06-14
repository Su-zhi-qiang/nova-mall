package com.su.mall.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.su.mall.common.service.RedisService;
import com.su.mall.mapper.*;
import com.su.mall.model.*;
import com.su.mall.portal.dao.HomeDao;
import com.su.mall.portal.domain.FlashPromotionProduct;
import com.su.mall.portal.domain.HomeContentResult;
import com.su.mall.portal.domain.HomeFlashPromotion;
import com.su.mall.portal.service.HomeService;
import com.su.mall.portal.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 首页内容管理Service实现类
 * @author Su
 */
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeServiceImpl.class);
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    private final SmsHomeAdvertiseMapper advertiseMapper;
    private final HomeDao homeDao;
    private final SmsFlashPromotionMapper flashPromotionMapper;
    private final SmsFlashPromotionSessionMapper promotionSessionMapper;
    private final SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;
    private final PmsProductMapper productMapper;
    private final PmsProductCategoryMapper productCategoryMapper;
    private final CmsSubjectMapper subjectMapper;
    private final RedisService redisService;

    @Override
    public HomeFlashPromotion getFlashPromotion() {
        Date now = new Date();
        SmsFlashPromotionSession currentSession = getFlashPromotionSession(now);

        if (currentSession == null) {
            LOGGER.info("当前无进行中的秒杀场次，尝试获取下一场预告");
            return buildEmptyFlashPromotionWithNext(now);
        }

        SmsFlashPromotion currentActivity = getCurrentActivity(now);
        if (currentActivity == null) {
            LOGGER.info("当前无有效秒杀活动");
            return buildEmptyFlashPromotionWithNext(now);
        }

        String cacheKey = "home:flashPromotion:" + currentActivity.getId() + ":" + currentSession.getId();
        HomeFlashPromotion cached = redisService.get(cacheKey);
        if (cached != null) {
            LOGGER.info("秒杀信息从缓存获取成功，cacheKey: {}", cacheKey);
            return cached;
        }

        String resetKey = "flash:reset:" + currentActivity.getId() + ":" + currentSession.getId();
        if (!redisService.hasKey(resetKey)) {
            try {
                int resetCount = flashPromotionProductRelationMapper.resetFlashStock(currentActivity.getId(), currentSession.getId());
                LOGGER.info("场次切换，已重置 {} 个秒杀商品的库存", resetCount);
            } catch (Exception e) {
                LOGGER.warn("重置秒杀库存失败（可能 original_count 字段尚未添加）：{}", e.getMessage());
            }
            redisService.set(resetKey, "1", 3600);
        }

        HomeFlashPromotion result = buildHomeFlashPromotion(currentActivity, currentSession, now);

        if (result.getProductList() != null && !result.getProductList().isEmpty()) {
            redisService.set(cacheKey, result, 300);
            LOGGER.info("秒杀信息已缓存，cacheKey: {}, 商品数量: {}", cacheKey, result.getProductList().size());
        } else {
            LOGGER.info("秒杀商品列表为空，未缓存");
        }

        return result;
    }

    @Override
    public HomeContentResult content() {
        String cacheKey = "home:content";
        // 先从缓存获取
        HomeContentResult cached = redisService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        HomeContentResult result = new HomeContentResult();
        //获取首页广告
        result.setAdvertiseList(getHomeAdvertiseList());
        //获取推荐品牌
        result.setBrandList(homeDao.getRecommendBrandList(0,6));
        //获取秒杀信息
        result.setHomeFlashPromotion(getFlashPromotion());
        //获取新品推荐
        result.setNewProductList(homeDao.getNewProductList(0,4));
        //获取人气推荐
        result.setHotProductList(homeDao.getHotProductList(0,4));
        //获取推荐专题
        result.setSubjectList(homeDao.getRecommendSubjectList(0,4));

        // 存入缓存，1小时过期
        redisService.set(cacheKey, result, 3600);
        return result;
    }

    @Override
    public Page<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum) {
        // TODO: 2019/1/29 暂时默认推荐所有商品
        Page<PmsProduct> page = new Page<>(pageNum, pageSize);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<PmsProduct>())
        return productMapper.selectPage(page,
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
    public Page<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum) {
        Page<CmsSubject> page = new Page<>(pageNum, pageSize);
        return subjectMapper.selectPage(page,
                new LambdaQueryWrapper<CmsSubject>()
                        .eq(CmsSubject::getShowStatus, 1)
                        .eq(cateId != null, CmsSubject::getCategoryId, cateId));
    }

    @Override
    public Page<PmsProduct> hotProductList(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null || pageNum < 1) {
            return new Page<>(1, 10);
        }
        int offset = pageSize * (pageNum - 1);
        List<PmsProduct> productList = homeDao.getHotProductList(offset, pageSize);
        if (productList == null) {
            productList = new ArrayList<>();
        }
        Page<PmsProduct> page = new Page<>(pageNum, pageSize);
        page.setRecords(productList);
        page.setTotal(productList.size());
        page.setPages(!productList.isEmpty() ? 1 : 0);
        return page;
    }

    @Override
    public Page<PmsProduct> newProductList(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null || pageNum < 1) {
            return new Page<>(1, 10);
        }
        int offset = pageSize * (pageNum - 1);
        List<PmsProduct> productList = homeDao.getNewProductList(offset, pageSize);
        if (productList == null) {
            productList = new ArrayList<>();
        }
        Page<PmsProduct> page = new Page<>(pageNum, pageSize);
        page.setRecords(productList);
        page.setTotal(productList.size());
        page.setPages(!productList.isEmpty() ? 1 : 0);
        return page;
    }

    private HomeFlashPromotion buildHomeFlashPromotion(SmsFlashPromotion activity, SmsFlashPromotionSession session, Date now) {
        HomeFlashPromotion homeFlashPromotion = new HomeFlashPromotion();

        long productCount = flashPromotionProductRelationMapper.selectCount(
            new LambdaQueryWrapper<SmsFlashPromotionProductRelation>()
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionId, activity.getId())
                .eq(SmsFlashPromotionProductRelation::getFlashPromotionSessionId, session.getId())
                .isNotNull(SmsFlashPromotionProductRelation::getFlashPromotionPrice)
                .gt(SmsFlashPromotionProductRelation::getFlashPromotionCount, 0));

        if (productCount <= 0) {
            LOGGER.info("活动 {} (ID:{}) 当前场次无有效商品", activity.getTitle(), activity.getId());
            return homeFlashPromotion;
        }

        Date startTimeToday = mergeDateAndTime(0, session.getStartTime());
        Date endTimeToday = mergeDateAndTime(0, session.getEndTime());

        homeFlashPromotion.setStartTime(startTimeToday);
        homeFlashPromotion.setEndTime(endTimeToday);

        SmsFlashPromotionSession nextSession = getNextFlashPromotionSession(now);
        if (nextSession != null) {
            homeFlashPromotion.setNextStartTime(mergeDateAndTime(1, nextSession.getStartTime()));
            homeFlashPromotion.setNextEndTime(mergeDateAndTime(1, nextSession.getEndTime()));
        }

        List<FlashPromotionProduct> flashProductList = homeDao.getFlashProductList(
            activity.getId(), session.getId());
        LOGGER.info("秒杀活动 {} 场次 {} 获取到 {} 个商品", activity.getId(), session.getId(),
            flashProductList != null ? flashProductList.size() : 0);

        homeFlashPromotion.setProductList(flashProductList);
        return homeFlashPromotion;
    }

    private HomeFlashPromotion buildEmptyFlashPromotionWithNext(Date now) {
        HomeFlashPromotion homeFlashPromotion = new HomeFlashPromotion();
        SmsFlashPromotionSession nextSession = getNextFlashPromotionSession(now);
        if (nextSession != null) {
            Date currTime = DateUtil.getTime(now);
            int dateOffset = nextSession.getStartTime().after(currTime) ? 0 : 1;
            homeFlashPromotion.setNextStartTime(mergeDateAndTime(dateOffset, nextSession.getStartTime()));
            homeFlashPromotion.setNextEndTime(mergeDateAndTime(dateOffset, nextSession.getEndTime()));
        }
        return homeFlashPromotion;
    }

    private SmsFlashPromotion getCurrentActivity(Date date) {
        List<SmsFlashPromotion> list = getFlashPromotionList(date);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 将日期部分和时间部分合并
     * 数据库中场次的时间字段（如 20:00:00）被读取后日期部分是 1970-01-01
     * 需要把日期部分改成指定日期（今天或明天），时间部分保留
     * @param dateOffset 0=今天, 1=明天
     * @param timeOnlyDate 只有时间部分的 Date（日期部分是1970-01-01）
     * @return 合并后的完整时间
     */
    private Date mergeDateAndTime(int dateOffset, Date timeOnlyDate) {
        if (timeOnlyDate == null) {
            return null;
        }

        // 获取今天的日期部分
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(new Date());
        // 加上偏移量（0=今天，1=明天）
        todayCal.add(Calendar.DAY_OF_MONTH, dateOffset);

        // 获取时间部分
        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(timeOnlyDate);

        // 合并：日期=今天（或明天），时间=场次时间
        todayCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        todayCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        todayCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
        todayCal.set(Calendar.MILLISECOND, 0);

        return todayCal.getTime();
    }

    //获取下一个场次信息
    private SmsFlashPromotionSession getNextFlashPromotionSession(Date date) {
        // 获取所有启用的场次
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotionSession>()
                        .eq(SmsFlashPromotionSession::getStatus, 1)
                        .orderByAsc(SmsFlashPromotionSession::getStartTime));
        
        if (CollectionUtils.isEmpty(promotionSessionList)) {
            return null;
        }
        
        // 获取当前时间（仅时间部分，日期设为1970-01-01）
        Date currTime = DateUtil.getTime(date);
        
        // 遍历场次，找到第一个开始时间晚于当前时间的场次
        for (SmsFlashPromotionSession session : promotionSessionList) {
            if (session.getStartTime().after(currTime)) {
                return session;
            }
        }
        
        // 如果今天所有场次都已结束，返回明天的第一个场次
        return promotionSessionList.get(0);
    }

    private List<SmsHomeAdvertise> getHomeAdvertiseList() {
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<SmsHomeAdvertise>())
        return advertiseMapper.selectList(
                new LambdaQueryWrapper<SmsHomeAdvertise>()
                        .eq(SmsHomeAdvertise::getType, 1)
                        .eq(SmsHomeAdvertise::getStatus, 1)
                        .orderByDesc(SmsHomeAdvertise::getSort));
    }

    //根据时间获取所有有效的秒杀活动
    private List<SmsFlashPromotion> getFlashPromotionList(Date date) {
        Date currDate = DateUtil.getDate(date);
        return flashPromotionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotion>()
                        .eq(SmsFlashPromotion::getStatus, 1)
                        .le(SmsFlashPromotion::getStartDate, currDate)
                        .ge(SmsFlashPromotion::getEndDate, currDate));
    }

    //根据时间获取秒杀场次
    private SmsFlashPromotionSession getFlashPromotionSession(Date date) {
        Date currTime = DateUtil.getTime(date);
        // ✅ 改造：selectByExample → selectList(new LambdaQueryWrapper<SmsFlashPromotionSession>())
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectList(
                new LambdaQueryWrapper<SmsFlashPromotionSession>()
                        .eq(SmsFlashPromotionSession::getStatus, 1)  // 只查询启用状态的场次
                        .le(SmsFlashPromotionSession::getStartTime, currTime)
                        .ge(SmsFlashPromotionSession::getEndTime, currTime));
        if (!CollectionUtils.isEmpty(promotionSessionList)) {
            return promotionSessionList.get(0);
        }
        return null;
    }
}