package com.ylpu.thales.scheduler.common.utils;

import java.util.Iterator;
import java.util.List;

public class StringUtils {

    private StringUtils() {

    }

    public static <T> String convertListAsString(List<T> list) {
        Iterator<T> it = list.iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
