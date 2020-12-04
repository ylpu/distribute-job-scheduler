package com.ylpu.thales.scheduler.enums;

public enum MisfirePolicy {

    NOTHING, IGNORE, PROCEED;

    public static MisfirePolicy getMisfirePolicy(String name) {
        for (MisfirePolicy policy : MisfirePolicy.values()) {
            if (policy.toString().equalsIgnoreCase(name)) {
                return policy;
            }
        }
        return IGNORE;
    }
}