package com.ylpu.thales.scheduler.alert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.google.common.eventbus.Subscribe;
import com.ylpu.thales.scheduler.core.alert.entity.Event;


public class EventListener {
	
	private static Log LOG = LogFactory.getLog(EventListener.class);
	
    @Subscribe  
    public void listen(Event event) {  
		AbstractMessageSend messageSender = MessageProvider.getAlert(event.getAlertType());
		try {
			messageSender.send(event);
		}catch(Exception e) {
			LOG.error(e);
		}
    } 
}
