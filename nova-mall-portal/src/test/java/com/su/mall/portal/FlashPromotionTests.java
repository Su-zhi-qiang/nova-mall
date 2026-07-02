package com.su.mall.portal;

import com.su.mall.mapper.SmsFlashPromotionDailyStockMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 秒杀功能测试
 * 
 */
@SpringBootTest
public class FlashPromotionTests {

    @Autowired
    private SmsFlashPromotionDailyStockMapper dailyStockMapper;

    /**
     * 测试秒杀库存扣减（每日快照表）
     */
    @Test
    public void testDecreaseStock() {
        // 假设秒杀关联ID为1，扣减数量为1
        int result = dailyStockMapper.decreaseStock(1L, 1);
        System.out.println("扣减结果：" + result);
        if (result > 0) {
            System.out.println("✅ 秒杀库存扣减成功");
        } else {
            System.out.println("❌ 秒杀库存不足");
        }
    }
}