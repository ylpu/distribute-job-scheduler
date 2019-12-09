package com.ylpu.thales.scheduler.enums;

public enum DBType {
    
    ORACLE, MYSQL, SQLSERVER;
    
    public static DBType getDBType(String name) {
        for(DBType dbType : DBType.values()) {
           if(dbType.toString().equalsIgnoreCase(name)) {
               return dbType;
           }
        }
        throw new IllegalArgumentException("unsupported db type " + name);
    }
}