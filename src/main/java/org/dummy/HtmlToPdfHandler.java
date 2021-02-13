package org.dummy;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.Headers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import static org.dummy.EmptinessUtils.isNotEmpty;
import static org.dummy.HtmlToPdfUtils.RESULT_PDF;
import static org.dummy.HtmlToPdfUtils.htmlToPdf;
import static org.dummy.OsUtils.*;

/**
 * HTML to PDF {@link HttpHandler}.
 * https://stackoverflow.com/a/60584801.
 */
public class HtmlToPdfHandler implements HttpHandler {

    private static final int INTERNAL_SERVER_ERROR = 500;
    public static final String TEXT_PLAIN = "text/plain";
    private static final String APPLICATION_PDF = "application/pdf";
    private static final String PDF_ATTACHED = "attachment;filename=\""+ RESULT_PDF +"\"";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws IOException {
        // https://stackoverflow.com/a/55070318
        // dispatch the request to a blocking thread
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        // Form data is stored here
        FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
        if (formData.iterator().hasNext()) {
            HtmlToPdfUtils.PrinterOptions po = new HtmlToPdfUtils.PrinterOptions();
            String url = exchange.getRequestURL();
            po.printoutSettings(url);
            po.buildOsCommandWrapper();
            createDirectory(po.getWorkdir());
            // Iterate through form data
            for (String data : formData) {
                for (FormData.FormValue formValue : formData.get(data)) {
                    if (formValue.isFileItem()) {
                        // Process file here
                        Path source = formValue.getFileItem().getFile();
                        Path target = po.getWorkdir().resolve(formValue.getFileName());
                        createFile(target);
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                        deleteFilesAndDirectories(source);
                    }
                }
            }
            htmlToPdf(po);
            Path resultPdf = po.getWorkdir().resolve(RESULT_PDF);
            if (resultPdf.toFile().exists() && resultPdf.toFile().isFile()) {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, APPLICATION_PDF);
                exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, PDF_ATTACHED);
                exchange.startBlocking();
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = Files.newInputStream(resultPdf);
                    outputStream = exchange.getOutputStream();
                    inputStream.transferTo(outputStream);
                } finally {
                    if (isNotEmpty(inputStream)) {
                        inputStream.close();
                    }
                    if (isNotEmpty(outputStream)) {
                        outputStream.close();
                    }
                }
            } else {
                exchange.setStatusCode(INTERNAL_SERVER_ERROR);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
                exchange.getResponseSender().send(
                        po.getWrapper().getOutputString() + DELIMITER_NEW_LINE + po.getWrapper().getErrorString()
                );
            }
            deleteFilesAndDirectories(po.getWorkdir());
        }
    }
}
