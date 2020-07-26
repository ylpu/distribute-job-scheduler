package com.ylpu.thales.scheduler.executor.log;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.xml.XmlConfiguration;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.ylpu.thales.scheduler.core.config.Configuration;

public class LogServer {

    private static Log LOG = LogFactory.getLog(LogServer.class);

    private static final int LOG_SERVER_PORT = 9098;

    private Server server = null;

    public LogServer(Properties prop) {
        int logServerPort = Configuration.getInt(prop, "thales.log.server.port", LOG_SERVER_PORT);
        server = new Server(logServerPort);
    }

    /**
     * 启动jetty服务器供客户端获取job的log信息
     * 
     * @param port
     * @throws Exception
     */
    public void startLogServer() throws Exception {
        XmlConfiguration config = new XmlConfiguration(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("jetty.xml"));
        config.configure(server);

        // static files handler
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase("./content");

        ContextHandler staticContext = new ContextHandler();
        staticContext.setHandler(resource_handler);

        // task handler
        ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass",
                "com.sun.jersey.api.core.PackagesResourceConfig");
        servletHolder.setInitParameter("com.sun.jersey.config.property.packages", "com.ylpu.thales.scheduler.executor.log");
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
            throw e;
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            LOG.error(e);
        }
    }
}
