package com.ylpu.thales.scheduler.enums;

public enum SQLOperator {

    SELECT, INSERT, UPDATE, DELETE;

    public static SQLOperator getSQLOperator(String name) {
        for (SQLOperator operator : SQLOperator.values()) {
            if (operator.toString().equalsIgnoreCase(name)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("unsupported sql operator " + name);
    }
}