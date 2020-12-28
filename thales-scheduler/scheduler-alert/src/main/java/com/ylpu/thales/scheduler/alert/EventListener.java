package com.ylpu.thales.scheduler.alert;

import com.google.common.eventbus.Subscribe;
import com.ylpu.thales.scheduler.core.alert.entity.Event;

public class EventListener {
    
    @Subscribe
    public void listen(Event event) throws Exception {
        AbstractMessageSend messageSender = MessageProvider.getAlert(event.getAlertType());
        messageSender.send(event);
    }
}
