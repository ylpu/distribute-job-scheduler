package com.ylpu.thales.scheduler.core.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;

public class CronUtils {

    public static String getLatestTriggerTime(String cron, Date from, Date to) {
        String dateStr = null;
        try {
            CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
            cronTriggerImpl.setCronExpression(cron);
            // 这里的时间是根据corn表达式算出来的值
            List<Date> dates = TriggerUtils.computeFireTimesBetween(cronTriggerImpl, null, from, to);
            Date date = dates.get(dates.size() - 1);
            dateStr = DateUtils.getDateAsString(date, DateUtils.TIME_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static String getNextTriggerTime(String cron, Date from, Date to) {
        String dateStr = null;
        try {
            CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
            cronTriggerImpl.setCronExpression(cron);
            // 这里的时间是根据corn表达式算出来的值
            List<Date> dates = TriggerUtils.computeFireTimesBetween(cronTriggerImpl, null, from, to);
            Date date = dates.get(0);
            dateStr = DateUtils.getDateAsString(date, DateUtils.DATE_TIME_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }
}