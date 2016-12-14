package com.google.gwt.sample.stockwatcher;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public class App {
    public static final String WEBAPP_RESOURCES_LOCATION = "webapp";
    static final int DEFAULT_PORT_STOP = 8090;
    static final String STOP_COMMAND = "stop";
    private static final int DEFAULT_PORT_START = 8080;
    private final int startPort;
    private final int stopPort;

    public App() {
        this(DEFAULT_PORT_START, DEFAULT_PORT_STOP);
    }

    public App(int startPort, int stopPort) {
        this.startPort = startPort;
        this.stopPort = stopPort;
    }

    static public void stop() {
        stop(DEFAULT_PORT_STOP);
    }

    static public void stop(Integer stopPort) {
        try {
            Socket s = new Socket(InetAddress.getByName("127.0.0.1"), stopPort);
            s.setSoLinger(false, 0);
            OutputStream out = s.getOutputStream();
            out.write(("stop\r\n").getBytes());
            out.flush();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        App jettyServer = null;
        if (args.length == 2) {
            jettyServer = new App(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
        } else {
            jettyServer = new App();
        }
        jettyServer.start();
    }

    public void start() throws Exception {
        Server server = new Server(startPort);
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(WEBAPP_RESOURCES_LOCATION + "/WEB-INF/web.xml");

        URL webAppDir = Thread.currentThread().getContextClassLoader().getResource(WEBAPP_RESOURCES_LOCATION);
        if (webAppDir == null) {
            throw new RuntimeException(String.format("No %s directory was found into the JAR file", WEBAPP_RESOURCES_LOCATION));
        }
        root.setResourceBase(webAppDir.toURI().toString());
        root.setParentLoaderPriority(true);

        server.setHandler(root);

        server.start();

        Monitor monitor = new Monitor(stopPort, new Server[]{server});
        monitor.start();

        server.join();
    }
}
