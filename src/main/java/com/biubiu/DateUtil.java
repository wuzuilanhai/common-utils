package com.biubiu;

import java.time.*;
import java.util.*;

/**
 * Created by Haibiao.Zhang on 2018/6/19.
 */
public class DateUtil {

    /**
     * 转换日期
     *
     * @param date 日期
     * @param hour 时间
     * @return 日期
     */
    public static Date transform(Date date, int hour) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDateTime localDateTime = localDate.atTime(LocalTime.of(hour, 0));
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 判断当前时间是否在[startTime, endTime)区间，注意时间格式要一致
     *
     * @param nowTime   当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 结果
     */
    public static boolean between(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()) {
            return true;
        }
        return nowTime.after(startTime) && nowTime.before(endTime);
    }

    /**
     * 判断时间是上午还是下午
     *
     * @param date 时间
     * @return 0-上午 1-下午
     */
    public static int judgeAmOrPm(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar.get(GregorianCalendar.AM_PM);
    }

    /**
     * 获取年月日格式的日期
     *
     * @param time 时长
     * @return 日期
     */
    public static Date getLocalDate(long time) {
        Date date = new Date(time);
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取时分秒的时长
     *
     * @param time 时间
     * @return 时长
     */
    public static Integer getLocalTime(long time) {
        Date end = new Date(time);
        Date start = getLocalDate(time);
        return Math.toIntExact(Duration.between(start.toInstant(), end.toInstant()).getSeconds());
    }

    /**
     * 获取两个日期之间的日期
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return 日期集合
     */
    public static List<Date> getBetweenDates(long start, long end) {
        Date startDate = getLocalDate(start);
        Date endDate = getLocalDate(end);
        List<Date> result = new ArrayList<>();
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(startDate);
        tempStart.add(Calendar.DAY_OF_YEAR, 0);

        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(endDate);
        while (tempStart.before(tempEnd)) {
            result.add(tempStart.getTime());
            tempStart.add(Calendar.DAY_OF_YEAR, 1);
        }
        result.add(tempEnd.getTime());
        return result;
    }

    /**
     * 获取当月第一天
     *
     * @param date 时间
     * @return 当月第一天
     */
    public static Date getDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

}
