package com.ylpu.thales.scheduler.rest;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.ylpu.thales.scheduler.core.config.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.util.Properties;

public class MasterRestServer {

    private static Log LOG = LogFactory.getLog(MasterRestServer.class);

    private static int MASTER_SERVICE_PORT = 9090;

    private Properties prop;

    Server server = null;
    
    public MasterRestServer() {
    }

    public MasterRestServer(Properties prop) {
        this.prop = prop;
    }

    public void startJettyServer() throws Exception {

        Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());

        int jettyServerPort = Configuration.getInt(prop, "thales.master.service.port", MASTER_SERVICE_PORT);

        server = new Server(jettyServerPort);

        XmlConfiguration config = new XmlConfiguration(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("jetty.xml"));
        config.configure(server);

        // static files handler
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase("./");

        ContextHandler staticContext = new ContextHandler();
        staticContext.setHandler(resource_handler);

        // task handler
        ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass",
                "com.sun.jersey.api.core.PackagesResourceConfig");
        servletHolder.setInitParameter("com.sun.jersey.config.property.packages",
                "com.ylpu.thales.scheduler.rest.resources");
        servletHolder.setAsyncSupported(true);
        ServletContextHandler taskContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        taskContext.addServlet(servletHolder, "/api/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(staticContext);
        handlers.addHandler(taskContext);

        server.setHandler(handlers);
        try {
            server.start();
        } catch (Exception e) {
            LOG.error(e);
            System.exit(1);
        }
    }

    private class ShutdownHookThread extends Thread {
        @Override
        public void run() {
            System.err.println("*** shutting down jetty server since JVM is shutting down");
            MasterRestServer.this.stop();
            System.err.println("*** server shut down");
        }
    }

    public void stop() {
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }
}
