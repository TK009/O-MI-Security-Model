package com.aaltoasia.omi.accontrol.http;

import com.aaltoasia.omi.accontrol.ACServlet;
import com.aaltoasia.omi.accontrol.PermissionService;
import com.aaltoasia.omi.accontrol.TestAuth;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by romanfilippov on 13/01/16.
 */
public class HttpServer implements Runnable
{
    int port;

    public HttpServer(int port){
        this.port = port;
    }

    public void run() {
        start();
    }

    private void start()
    {
        //Server Setup
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        // The filesystem paths we will map
        String homePath = System.getProperty("user.home");
        String pwdPath = System.getProperty("user.dir");

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setResourceBase(pwdPath);
        context.setContextPath("/");
        server.setHandler(context);

        // add a simple Servlet at "/dynamic/*"
//        ServletHolder holderDynamic = new ServletHolder("dynamic", DynamicServlet.class);
//        context.addServlet(holderDynamic, "/dynamic/*");

        ServletHolder holderHome = new ServletHolder("static-home", DefaultServlet.class);
        holderHome.setInitParameter("resourceBase",pwdPath);
        holderHome.setInitParameter("dirAllowed","true");
        holderHome.setInitParameter("pathInfoOnly","true");
        context.addServlet(holderHome,"/AC/*");

        ServletHolder permissionService = new ServletHolder(new PermissionService());
        context.addServlet(permissionService, "/PermissionService/*");

        ServletHolder testAuth = new ServletHolder(new TestAuth());
        context.addServlet(testAuth, "/O-MI/*");

        // Lastly, the default servlet for root content (always needed, to satisfy servlet spec)
        // It is important that this is last.
        ServletHolder holderPwd = new ServletHolder("default", DefaultServlet.class);
        holderPwd.setInitParameter("dirAllowed","true");
        context.addServlet(holderPwd,"/");

        try
        {
            server.start();
            server.join();
        }
        catch (Throwable t)
        {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, t.getMessage());
        }
    }

    public static void main(String[] args) {
        //Start HTTP Server
        (new Thread(new HttpServer(8088))).start();
    }
}
