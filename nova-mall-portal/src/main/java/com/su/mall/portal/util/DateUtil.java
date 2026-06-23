package com.su.mall.portal.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 * @author Su
 */
public class DateUtil {

    private DateUtil() {
    }

    /**
     * 将日期部分和时间部分合并
     * 数据库中场次的时间字段（如 20:00:00）被读取后日期部分是 1970-01-01
     * 需要把日期部分改成指定日期（今天或明天），时间部分保留
     *
     * @param dateOffset 0=今天, 1=明天
     * @param timeOnlyDate 只有时间部分的 Date（日期部分是1970-01-01）
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
     * 获取日期部分（年-月-日 00:00:00）
     */
    public static Date getDatePart(Date date) {
        return getDate(date);
    }

    /**
     * 获取日期部分（年-月-日 00:00:00）
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
     * 获取时间部分（1970-01-01 HH:mm:ss）
     */
    public static Date getTimePart(Date date) {
        return getTime(date);
    }

    /**
     * 获取时间部分（1970-01-01 HH:mm:ss）
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
