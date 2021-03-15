package org.dummy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import static org.dummy.HtmlToPdfUtils.PrinterOptions.TMP_DIR;
import static org.dummy.OsUtils.deleteFilesAndDirectories;

/**
 * Main class.
 */
public class App {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        deleteFilesAndDirectories(TMP_DIR);
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new CommonHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}