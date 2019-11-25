package com.ylpu.thales.scheduler.alert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.ylpu.thales.scheduler.core.alert.entity.Event;
import com.ylpu.thales.scheduler.enums.AlertType;
import com.ylpu.thales.scheduler.enums.EventType;

public class EventPublsher {
	
	private static Log LOG = LogFactory.getLog(EventPublsher.class);

	public void publish(Event event) {
		AbstractMessageSend messageSender = MessageProvider.getAlert(event.getAlertType());
		try {
			messageSender.send(event);
		}catch(Exception e) {
			LOG.error(e);
		}
	}
	
	public static void main(String[] args) {
		EventPublsher eventPublsher = new EventPublsher();
		Event event = new Event();
		event.setTaskId(12);
		event.setHostName("loalhost");
		event.setLogUrl("http://localhost:8090/api/task/1");
		event.setAlertType(AlertType.EMAIL);
		event.setAlertUsers("username@163.com");
		event.setEventType(EventType.TASKFAIL);
		eventPublsher.publish(event);
	}
}
