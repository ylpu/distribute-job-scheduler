package com.ylpu.thales.scheduler.master.context.parameter;

import java.util.HashMap;
import java.util.Map;

import com.ylpu.thales.scheduler.core.utils.DateUtils;

public class ContextParameterProvider {
    
    private static Map<String,Object> contextParameter = new HashMap<String,Object>();
    
    static {
        contextParameter.put("today", DateUtils.getToday());
        contextParameter.put("yesterday", DateUtils.getYesterday());
        contextParameter.put("tomorrow", DateUtils.getTomorrow());
        contextParameter.put("week_start", DateUtils.getWeekStart());
        contextParameter.put("week_end", DateUtils.getWeekEnd());
        contextParameter.put("month_start", DateUtils.getMonthStart());
        contextParameter.put("month_end", DateUtils.getMonthEnd());
        contextParameter.put("year_start", DateUtils.getYearStart());
        contextParameter.put("year_end", DateUtils.getYearEnd());
    }
    
    public static Object getParameterValue(String key) {
        return contextParameter.get(key);
    }
    
    public static Map<String,Object> getContextParameter() {
        return contextParameter;
    }
}
