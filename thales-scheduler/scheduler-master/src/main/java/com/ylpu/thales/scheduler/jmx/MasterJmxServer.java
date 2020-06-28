package com.ylpu.thales.scheduler.jmx;

import com.sun.jdmk.comm.HtmlAdaptorServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import static com.google.common.base.Preconditions.checkNotNull;

public class MasterJmxServer {
    private static Log LOG = LogFactory.getLog(MasterJmxServer.class);

    private Integer jmxPort;
    private HtmlAdaptorServer adapter = null;

    public MasterJmxServer(Integer jmxPort) {
        this.jmxPort = checkNotNull(jmxPort, "JMX Port cannot be null");
    }

    public void start(){
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());

            MBeanServer server = MBeanServerFactory.createMBeanServer();
            ObjectName jobResource = new ObjectName("com.ylpu.thales.scheduler:name=MasterManager");
            server.registerMBean(new MasterMetric(), jobResource);
            ObjectName adapterName = new ObjectName("ResourceAgent:name=htmladapter,port=9095");
            adapter = new HtmlAdaptorServer();
            server.registerMBean(adapter, adapterName);
            adapter.setPort(jmxPort);
            adapter.start();
        }catch(Exception e) {
            LOG.error(e);
            System.exit(1);
        }
    }

    private class ShutdownHookThread extends Thread {
        @Override
        public void run() {
            LOG.error("*** shutting down jmx server since JVM is shutting down");
            MasterJmxServer.this.stop();
            LOG.error("*** server shut down");
        }
    }

    public void stop() {
        if (adapter != null) {
            adapter.stop();
        }
    }
}
