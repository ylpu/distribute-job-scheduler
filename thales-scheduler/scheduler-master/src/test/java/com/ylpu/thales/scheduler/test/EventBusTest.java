package com.ylpu.thales.scheduler.test;

import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;
import com.ylpu.thales.scheduler.alert.EventListener;
import com.ylpu.thales.scheduler.core.alert.entity.Event;
import com.ylpu.thales.scheduler.enums.AlertType;
import com.ylpu.thales.scheduler.enums.EventType;

public class EventBusTest {
    
    private static AsyncEventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(1));

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        eventBus.register(new EventListener());
        Event event = new Event();
        event.setTaskId(123);
        event.setAlertType(AlertType.EMAIL);
        event.setAlertUsers("test");
        event.setLogUrl("http://localhost:8090/123");
        event.setHostName("localhost");
        event.setEventType(EventType.TIMEOUT);
        eventBus.post(event);
    }

}
