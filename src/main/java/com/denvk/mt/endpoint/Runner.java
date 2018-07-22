package com.denvk.mt.endpoint;

import com.denvk.mt.StartupConfig;
import java.net.BindException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author Denis Voroshchuk
 */
public class Runner {

    private static final Logger logger = LogManager.getLogger(Runner.class);

    public static void main(String[] args) throws Exception {
        Runner runner = new Runner();
        StartupConfig config = StartupConfig.load(args);
        if (config == null) {
            //help is shown
            System.out.println(USAGE);
            return;
        }
        Server server = runner.createServer(config);
        if (server == null) {
            return;
        }
        try {
            server.start();
            server.join();
        } catch (BindException e) {
            logger.fatal(String.format("Can't start server. Probably the port %d is busy.\n"
                    + "Try to start program with different port using input parameters:\n"
                    + "-port %d", config.getPort(),config.getPort()+1), e);
        } catch (Throwable t) {
            logger.fatal("Error during server run.", t);
        } finally {
            server.stop();
        }
    }

    public Server createServer(StartupConfig config) {
        try {
            Server server = new Server(config.getPort());
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
            context.setContextPath("/");
            server.setHandler(context);
            ServletHolder servlet = context.addServlet(
                    org.glassfish.jersey.servlet.ServletContainer.class, "/*");
            servlet.setInitOrder(0);
            servlet.setInitParameter(
                    "jersey.config.server.provider.classnames",
                    AccountEndpoint.class.getCanonicalName());
            return server;
        } catch (Throwable t) {
            logger.fatal("Error occurs during server configuration.", t);
            return null;
        }
    }

    public static final String USAGE = "[-port PORT_NUMBER][-help][/?]\n"
            + "Default PORT_NUMBER is 8080\n"
            + "example:\n\tport 8080\n";
}
