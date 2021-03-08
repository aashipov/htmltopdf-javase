package org.dummy;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerFileUpload;
import java.nio.file.Path;
import static org.dummy.EmptinessUtils.isNotEmpty;

/**
 * {@link HttpServerFileUpload} {@link Handler}.
 */
public class UploadHandler implements Handler<HttpServerFileUpload> {

    private final HtmlToPdfUtils.PrinterOptions po;

    /**
     * Constructor.
     * @param p {@link HtmlToPdfUtils.PrinterOptions}
     */
    public UploadHandler(HtmlToPdfUtils.PrinterOptions p) {
        this.po = p;
    }

    @Override
    public void handle(HttpServerFileUpload event) {
        if (isNotEmpty(event.filename())) {
            Path file = po.getWorkdir().resolve(event.filename());
            event.streamToFileSystem(file.toString());
        }
    }
}
