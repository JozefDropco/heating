package org.dropco.smarthome.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class WebServer {

    private final Server server;

    public WebServer() {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server = new Server(8080);

        ResourceConfig application = new ResourceConfig()
                .packages(true,"org.dropco.smarthome.web");
        ServletHolder servlet = new ServletHolder(new
                ServletContainer(application));
        servlet.setInitOrder(0);

        context.addServlet(servlet, "/*");
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase(".");

        HandlerList container = new HandlerList(new CORSFilter(),resource_handler,context);
        server.setHandler(container);
        server.dumpStdErr();

    }

    public void start() throws Exception {
        server.start();
    }
    public void join() throws InterruptedException {
        server.join();
    }

    public static void main(String[] args) throws Exception {
        new WebServer().start();
    }
}
