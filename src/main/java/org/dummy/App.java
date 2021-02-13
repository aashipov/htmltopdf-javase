package org.dummy;

import io.undertow.Undertow;

/**
 * Main class.
 */
public class App {

    private static final String HOST = "0.0.0.0";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(PORT, HOST)
                .setHandler(new CommonHandler()).build();
        server.start();
    }

}