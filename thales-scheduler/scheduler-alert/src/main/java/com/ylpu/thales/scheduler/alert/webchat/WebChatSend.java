package com.ylpu.thales.scheduler.alert.webchat;

import java.util.Properties;

import com.ylpu.thales.scheduler.alert.AbstractMessageSend;
import com.ylpu.thales.scheduler.core.alert.entity.Event;

public class WebChatSend extends AbstractMessageSend {

    public static final String TEMPLATE_PATH = "templates/webchat-alert-template.vm";

    private Properties prop;

    public WebChatSend(Properties prop) {
        this.prop = prop;
    }

    public void send(Event event) {

    }
}