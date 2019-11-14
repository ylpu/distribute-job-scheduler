package com.ylpu.thales.scheduler.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    public static final String TIME_FORMAT = "yyyyMMddHHmmss";
	
    private DateUtils() {
		
    }
	
    public static String getDateAsString(Date date,String format) {		
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
    
    public static Date getDateFromString(String dateStr,String format) {     
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static int getElapseTime(Date startTime,Date endTime) {
        Long time = (endTime.getTime() - startTime.getTime()) / 1000;
        return time.intValue();
    }
}
