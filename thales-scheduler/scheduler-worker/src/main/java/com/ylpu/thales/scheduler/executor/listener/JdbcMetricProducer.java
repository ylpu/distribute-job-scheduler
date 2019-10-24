package com.ylpu.thales.scheduler.executor.listener;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JdbcMetricProducer implements IMetricProducer{
    
    private Properties props;
    
    private static Log LOG = LogFactory.getLog(JdbcMetricProducer.class);
    
    public JdbcMetricProducer(Properties props) {
        this.props = props;
    }
    
    public void send(Event event) {
 
    }
}
