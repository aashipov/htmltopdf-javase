package org.dummy;

import io.undertow.Undertow;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;

/**
 * Main class.
 */
public class App {

    private static final String host = "0.0.0.0";
    private static final int port = 8080;

    public static void main(String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(exchange -> {
                    if (exchange.getRequestURL().contains("html") || exchange.getRequestURL().contains("chromium")) {
                        // Parses HTTP POST form data and passes it to a handler asynchronously
                        FormDataParser parser = FormParserFactory.builder().build().createParser(exchange);
                        HtmlToPdfHandler handler = new HtmlToPdfHandler();
                        parser.parse(handler);
                    } else {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("{\"status\":\"UP\"}");
                    }
                }).build();
        server.start();
    }

}