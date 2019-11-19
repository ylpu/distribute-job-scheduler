package com.ylpu.thales.scheduler.enums;

public enum AlertType {
    
    SMS(1), WEBCHAT(2), EMAIL(3);
    
    private int code;
    
    private AlertType(int code) {
        this.code = code;
    }
    
    public static AlertType getAlertType(int code) {
        for(AlertType alertType : AlertType.values()) {
           if(alertType.code == code) {
               return alertType;
           }
        }
        throw new IllegalArgumentException("unsupported alert type " + code);
    }
    
    public static AlertType getAlertType(String name) {
        for(AlertType alertType : AlertType.values()) {
           if(alertType.toString().equalsIgnoreCase(name)) {
               return alertType;
           }
        }
        throw new IllegalArgumentException("unsupported alert type " + name);
    }
    
    public int getCode() {
        return this.code;
    }
}