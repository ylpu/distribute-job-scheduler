package com.ylpu.thales.scheduler.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import com.google.protobuf.Timestamp;
import com.ylpu.thales.scheduler.enums.JobCycle;

public class DateUtils {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String MINUTE_TIME_FORMAT = "yyyyMMddHHmm00";
    
    public static final String TIME_FORMAT = "yyyyMMddHHmmss";
    
    public static final String DATE_FORMAT = "yyyyMMdd";

    private DateUtils() {

    }

    public static String getDateAsString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static Date getDateFromString(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Timestamp getProtobufTime() {
        return getProtobufTime(new Date());
    }

    public static Timestamp getProtobufTime(Date date) {
        return Timestamp.newBuilder().setSeconds(date.getTime() / 1000).build();
    }

    public static Date getDatetime(Timestamp ts) {
        return new Date(ts.getSeconds() * 1000);
    }

    public static int getElapseTime(Date startTime, Date endTime) {
        Long time = (endTime.getTime() - startTime.getTime()) / 1000;
        return time.intValue();
    }

    public static Date getTime(Date date, int cycle, int step) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        JobCycle jobCycle = JobCycle.getJobCycle(cycle);

        switch (jobCycle) {

        case MINUTE:
            calendar.add(Calendar.MINUTE, step * 60);
            break;
        case HOUR:
            calendar.add(Calendar.HOUR, step * 24);
            break;
        case DAY:
            calendar.add(Calendar.DAY_OF_MONTH, step * 31);
            break;
        case WEEK:
            calendar.add(Calendar.WEEK_OF_MONTH, step * 4);
            break;
        case MONTH:
            calendar.add(Calendar.MONTH, step * 12);
            break;
        case YEAR:
            calendar.add(Calendar.YEAR, step * 10);
            break;
        }
        return calendar.getTime();
    }
    
    public static String getToday() {
        return getDateAsString(new Date(),DATE_FORMAT);
    }
    
    public static String getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return getDateAsString(calendar.getTime(),DATE_FORMAT);
    }
    
    public static String getTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        return getDateAsString(calendar.getTime(),DATE_FORMAT);
    }
    
    public static String getWeekStart(){
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_MONTH, 0);
        cal.set(Calendar.DAY_OF_WEEK, 2);
        Date time=cal.getTime();
        return getDateAsString(time,DATE_FORMAT);
    }
    
    public static String getWeekEnd(){
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK));
        cal.add(Calendar.DAY_OF_WEEK, 1);
        Date time=cal.getTime();
        return getDateAsString(time,DATE_FORMAT);
    }
    
    public static String getMonthStart(){
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date time=cal.getTime();
        return getDateAsString(time,DATE_FORMAT);
    }
    
    public static String getMonthEnd(){
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date time=cal.getTime();
        return getDateAsString(time,DATE_FORMAT);
    }
    
    public static String getYearStart(){
        return new SimpleDateFormat("yyyy").format(new Date())+"0101";
    }
    
    public static String getYearEnd(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH,calendar.getActualMaximum(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date currYearLast = calendar.getTime();
        return getDateAsString(currYearLast,DATE_FORMAT);
    }
}
