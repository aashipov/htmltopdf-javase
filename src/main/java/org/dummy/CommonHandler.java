package org.dummy;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import io.undertow.util.Methods;

import static org.dummy.HtmlToPdfHandler.TEXT_PLAIN;

/**
 * Common {@link HttpHandler}.
 */
public class CommonHandler  implements HttpHandler {

    private static final String HTML = "html";
    private static final String CHROMIUM = "chromium";
    private static final String STATUS_UP = "{\"status\":\"UP\"}";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if ((exchange.getRequestURL().contains(HTML) || exchange.getRequestURL().contains(CHROMIUM))
                && exchange.getRequestMethod().equals(Methods.POST)) {
            // Parses HTTP POST form data and passes it to a handler asynchronously
            FormDataParser parser = FormParserFactory.builder().build().createParser(exchange);
            HtmlToPdfHandler handler = new HtmlToPdfHandler();
            parser.parse(handler);
        } else {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
            exchange.getResponseSender().send(STATUS_UP);
        }
    }
}
