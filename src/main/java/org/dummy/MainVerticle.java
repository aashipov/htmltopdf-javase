package org.dummy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * {@link AbstractVerticle} implementation.
 * https://github.com/agrajm/vertx-fileupload/blob/master/src/main/java/com/examples/vertx/SimpleFormUploadServer.java
 * https://stackoverflow.com/questions/36401510/handling-multipart-form-in-vertx
 */
public class MainVerticle extends AbstractVerticle {

    private static final int PORT = 8080;

    @Override
    public void start(Promise<Void> startPromise) {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setMergeFormAttributes(true));
        router.route().handler(new CommonHandler());
        server.requestHandler(router);
        server.listen(PORT);
    }
}
