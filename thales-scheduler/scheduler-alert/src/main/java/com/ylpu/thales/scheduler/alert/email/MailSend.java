package com.ylpu.thales.scheduler.alert.email;

import java.text.MessageFormat;
import java.util.Properties;
import com.ylpu.thales.scheduler.alert.AbstractMessageSend;
import com.ylpu.thales.scheduler.core.alert.entity.Event;

public class MailSend extends AbstractMessageSend {

    public static final String TEMPLATE_PATH = "templates/mail-alert-template.vm";

    private Properties prop;

    public MailSend(Properties prop) {
        this.prop = prop;
    }

    public void send(Event event) throws Exception{
        MailSenderInfo mailInfo = new MailSenderInfo();
        buidMailHeader(mailInfo,event);
        mailInfo.setToAddress(event.getAlertUsers());
        mailInfo.setContent(buildMessageBody(event, TEMPLATE_PATH));
        SimpleMailSender.sendHtmlMail(mailInfo);
    }

    private void buidMailHeader(MailSenderInfo mailInfo,Event event) {
        mailInfo.setMailServerHost(prop.getProperty("MAIL_SERVER_HOST"));
        mailInfo.setMailServerPort(prop.getProperty("MAIL_SERVER_PORT"));
        mailInfo.setValidate(Boolean.TRUE);
        mailInfo.setUserName(prop.getProperty("MAIL_USER_NAME"));
        mailInfo.setPassword(prop.getProperty("MAIL_PASSWORD"));
        mailInfo.setFromAddress(prop.getProperty("MAIL_SEND_FROM"));
        mailInfo.setSubject(MessageFormat.format(prop.getProperty("MAIL_SUBJECT"),event.getEventType()).toLowerCase());
    }
}