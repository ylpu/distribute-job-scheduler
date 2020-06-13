package com.ylpu.thales.scheduler.alert;

import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import com.ylpu.thales.scheduler.core.alert.entity.Event;

public abstract class AbstractMessageSend {

    public abstract void send(Event event) throws Exception;

    public String buildMessageBody(Event event, String templateName) {
        VelocityEngine ve = new VelocityEngine();
        initClassLoader(ve);
        Template t = ve.getTemplate(templateName, "UTF-8");

        VelocityContext context = new VelocityContext();
        addAlertParameters(context, event);

        /* now render the template into a StringWriter */
        StringWriter out = new StringWriter();
        t.merge(context, out);
        return out.toString();
    }

    public void initClassLoader(VelocityEngine ve) {
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init(p);
    }

    public void addAlertParameters(VelocityContext context, Event event) {
        context.put("taskId", event.getTaskId());
        context.put("logUrl", event.getLogUrl());
        context.put("hostName", event.getHostName());
        context.put("eventType", event.getEventType().toString());
    }
}
