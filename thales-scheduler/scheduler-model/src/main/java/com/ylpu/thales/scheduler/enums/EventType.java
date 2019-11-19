package com.ylpu.thales.scheduler.enums;

public enum EventType {
    
    TIMEOUT(1), NODELOST(2), TASKFAIL(3);
    
    private int code;
    
    private EventType(int code) {
        this.code = code;
    }
    
    public static EventType getEventType(int code) {
        for(EventType eventType : EventType.values()) {
           if(eventType.code == code) {
               return eventType;
           }
        }
        throw new IllegalArgumentException("unsupported event type " + code);
    }
    
    public static EventType getEventType(String name) {
        for(EventType eventType : EventType.values()) {
           if(eventType.toString().equalsIgnoreCase(name)) {
               return eventType;
           }
        }
        throw new IllegalArgumentException("unsupported event type " + name);
    }
    
    public int getCode() {
        return this.code;
    }
}