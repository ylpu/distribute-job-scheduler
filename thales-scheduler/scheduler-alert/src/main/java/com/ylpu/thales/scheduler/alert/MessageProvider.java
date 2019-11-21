package com.ylpu.thales.scheduler.alert;
import java.util.concurrent.ConcurrentHashMap;
import com.ylpu.thales.scheduler.alert.email.MailSend;
import com.ylpu.thales.scheduler.alert.sms.SMSSend;
import com.ylpu.thales.scheduler.alert.webchat.WebChatSend;
import com.ylpu.thales.scheduler.core.config.Configuration;
import com.ylpu.thales.scheduler.enums.AlertType;
public class MessageProvider {
	
   private static ConcurrentHashMap<AlertType,AbstractMessageSend> map = new ConcurrentHashMap<AlertType,AbstractMessageSend>();
   static {
      map.put(AlertType.EMAIL, new MailSend(Configuration.getConfigFile("email.properties")));
      map.put(AlertType.SMS, new SMSSend(Configuration.getConfigFile("sms.properties")));
      map.put(AlertType.WEBCHAT, new WebChatSend(Configuration.getConfigFile("webchat.properties")));
   }
	
   public static AbstractMessageSend getAlert(AlertType alertType){
      return map.get(alertType);
   }
}
