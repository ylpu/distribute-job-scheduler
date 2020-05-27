package com.ylpu.thales.scheduler.alert.sms;

import java.util.Properties;

import com.ylpu.thales.scheduler.alert.AbstractMessageSend;
import com.ylpu.thales.scheduler.core.alert.entity.Event;

public class SMSSend extends AbstractMessageSend {

    public static final String TEMPLATE_PATH = "templates/sms-alert-template.vm";

    private Properties prop;

    public SMSSend(Properties prop) {
        this.prop = prop;
    }

    public void send(Event event) {

    }
}