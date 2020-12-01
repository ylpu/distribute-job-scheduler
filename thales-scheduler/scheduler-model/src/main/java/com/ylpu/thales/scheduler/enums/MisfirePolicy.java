package com.ylpu.thales.scheduler.enums;

public enum MisfirePolicy {

    NOTHING, IGONRE, PROCEED;

    public static MisfirePolicy getMisfirePolicy(String name) {
        for (MisfirePolicy operator : MisfirePolicy.values()) {
            if (operator.toString().equalsIgnoreCase(name)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("unsupported sql operator " + name);
    }
}