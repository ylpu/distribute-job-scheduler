package com.ylpu.thales.scheduler.core.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;

public class CronUtils {
    
    private static Log LOG = LogFactory.getLog(CronUtils.class);

    public static String getLatestTriggerTime(String cron, Date from, Date to) {
        String dateStr = null;
        try {
            CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
            cronTriggerImpl.setCronExpression(cron);
            List<Date> dates = TriggerUtils.computeFireTimesBetween(cronTriggerImpl, null, from, to);
            Date date = dates.get(dates.size() - 1);
            dateStr = DateUtils.getDateAsString(date, DateUtils.MINUTE_TIME_FORMAT);
        } catch (ParseException e) {
            LOG.error(e);
        }
        return dateStr;
    }

    public static String getNextTriggerTimeAsString(String cron, Date from, Date to) {
        String dateStr = null;
        try {
            Date date = getNextTriggerTime(cron,from,to);
            dateStr = DateUtils.getDateAsString(date, DateUtils.DATE_TIME_FORMAT);
        } catch (Exception e) {
            LOG.error(e);
        }
        return dateStr;
    }
    
    public static Date getNextTriggerTime(String cron, Date from, Date to) {
        Date date = null;
        try {
            CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
            cronTriggerImpl.setCronExpression(cron);
            List<Date> dates = TriggerUtils.computeFireTimesBetween(cronTriggerImpl, null, from, to);
            date = dates.get(0);
        } catch (ParseException e) {
            LOG.error(e);
        }
        return date;
    }
    public static void main(String[] args) {
        Date date = DateUtils.getDateFromString("2020-06-21 09:37:00", DateUtils.DATE_TIME_FORMAT);
        System.out.println(getLatestTriggerTime("0 */10 * * * ?",
                DateUtils.getTime(date, 1, -1), date));
    }
}