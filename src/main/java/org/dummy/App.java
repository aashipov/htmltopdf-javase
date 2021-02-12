package org.dummy;

import io.undertow.Undertow;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import static org.dummy.HtmlToPdfHandler.TEXT_PLAIN;

/**
 * Main class.
 */
public class App {

    private static final String HOST = "0.0.0.0";
    private static final int PORT = 8080;
    private static final String HTML = "html";
    private static final String CHROMIUM = "chromium";
    private static final String STATUS_UP = "{\"status\":\"UP\"}";

    public static void main(String[] args) {
        Undertow server = Undertow.builder()
                .addHttpListener(PORT, HOST)
                .setHandler(exchange -> {
                    if (exchange.getRequestURL().contains(HTML) || exchange.getRequestURL().contains(CHROMIUM)) {
                        // Parses HTTP POST form data and passes it to a handler asynchronously
                        FormDataParser parser = FormParserFactory.builder().build().createParser(exchange);
                        HtmlToPdfHandler handler = new HtmlToPdfHandler();
                        parser.parse(handler);
                    } else {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
                        exchange.getResponseSender().send(STATUS_UP);
                    }
                }).build();
        server.start();
    }

}