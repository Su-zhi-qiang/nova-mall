package com.su.mall.portal.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 * <p>提供日期和时间的拆分、合并操作
 * <p>主要解决数据库TIME类型字段读取后日期部分为1970-01-01的问题
 *
 * @see com.su.mall.portal.component.HomeServiceImpl#getCurrentFlashPromotion
 */
public class DateUtil {

    private DateUtil() {
    }

    /**
     * 将日期偏移和时间部分合并为完整时间
     * <p>数据库TIME字段（如20:00:00）读取后日期部分是1970-01-01
     * <p>此方法将日期部分替换为今天或明天，时间部分保留
     *
     * @param dateOffset    日期偏移量：0=今天, 1=明天
     * @param timeOnlyDate  只含时间部分的Date（日期部分为1970-01-01）
     * @return 合并后的完整时间
     */
    public static Date mergeDateAndTime(int dateOffset, Date timeOnlyDate) {
        if (timeOnlyDate == null) {
            return null;
        }

        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(new Date());
        todayCal.add(Calendar.DAY_OF_MONTH, dateOffset);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(timeOnlyDate);

        todayCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        todayCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        todayCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
        todayCal.set(Calendar.MILLISECOND, 0);

        return todayCal.getTime();
    }

    /**
     * 提取日期部分（年-月-日 00:00:00），忽略时间
     *
     * @param date 原始日期
     * @return 日期部分（时间归零）
     */
    public static Date getDatePart(Date date) {
        return getDate(date);
    }

    /**
     * 提取日期部分（年-月-日 00:00:00），忽略时间
     */
    public static Date getDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 提取时间部分（1970-01-01 HH:mm:ss），忽略日期
     *
     * @param date 原始日期
     * @return 时间部分（日期固定为1970-01-01）
     */
    public static Date getTimePart(Date date) {
        return getTime(date);
    }

    /**
     * 提取时间部分（1970-01-01 HH:mm:ss），忽略日期
     */
    public static Date getTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }
}
