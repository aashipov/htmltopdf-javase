package org.dummy;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.dummy.HtmlToPdfUtils.INDEX_HTML;
import static org.dummy.HtmlToPdfUtils.RESULT_PDF;
import static org.dummy.OsUtils.*;

/**
 * HTML {@link HttpHandler}.
 * Simplified https://gist.github.com/JensWalter/0f19780d131d903879a2.
 */
public class CommonHandler implements HttpHandler {

    private static final Logger log = Logger.getLogger(CommonHandler.class.getSimpleName());
    private static final String APPLICATION_PDF = "application/pdf";
    private static final String PDF_ATTACHED = "attachment;filename=\"" + RESULT_PDF + "\"";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String TEXT_PLAIN = "text/plain; charset=" + DEFAULT_CHARSET_NAME;
    private static final String STATUS_UP = "{\"status\":\"UP\"}";
    private static final String BOUNDARY = "boundary=";
    private static final String FILES = "\"" + "files" + "\"";
    private static final String DELIMITER_CARRIAGE_RETURN_AND_NEW_LINE = "\r" + DELIMITER_NEW_LINE;
    private static final byte[] DOUBLE_DELIMITER =
            (DELIMITER_CARRIAGE_RETURN_AND_NEW_LINE + DELIMITER_CARRIAGE_RETURN_AND_NEW_LINE).getBytes(DEFAULT_CHARSET);
    private static final String FILENAME_EQUALS = "filename=";
    private static final String FILENAME_LOOKUP = "; " + FILENAME_EQUALS;

    private static final String FILENAME_HEADER_LOOKUP = DELIMITER_CARRIAGE_RETURN_AND_NEW_LINE + CONTENT_DISPOSITION + ": form-data; name=" + FILES + FILENAME_LOOKUP;
    private static final String RFC_7578_PREPEND = "\r\n--";
    private static final int OK = 200;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final String CHROMIUM = "chromium";
    private static final String HTML = "html";
    private static final String MULTIPART = "multipart/form-data";

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String url = httpExchange.getRequestURI().toString();
        if (url.contains(CHROMIUM) || url.contains(HTML)) {
            Headers headers = httpExchange.getRequestHeaders();
            String contentType = headers.getFirst(CONTENT_TYPE);
            if (contentType.startsWith(MULTIPART)) {
                //found form data
                String boundary = contentType.substring(contentType.indexOf(BOUNDARY) + BOUNDARY.length());
                byte[] boundaryBytes = (boundary).getBytes(DEFAULT_CHARSET);
                byte[] payload = requestBody(httpExchange);
                ArrayList<MultiPart> list = new ArrayList<>();
                List<Integer> offsets = indexesOf(payload, boundaryBytes);
                for (int idx = 0; idx < offsets.size(); idx++) {
                    int startPart = offsets.get(idx);
                    int endPart = payload.length;
                    if (idx < offsets.size() - 1) {
                        endPart = offsets.get(idx + 1) - RFC_7578_PREPEND.length();
                    }
                    byte[] part = Arrays.copyOfRange(payload, startPart, endPart);
                    //look for header
                    List<Integer> headerEnds = indexesOf(part, DOUBLE_DELIMITER);
                    int headerEnd = !headerEnds.isEmpty() ? headerEnds.get(0) : -1;
                    if (headerEnd > 0) {
                        MultiPart p = new MultiPart();
                        byte[] head = Arrays.copyOfRange(part, 0, headerEnd);
                        String header = new String(head);
                        // extract name from header
                        int nameIndex = header.indexOf(FILENAME_HEADER_LOOKUP);
                        if (nameIndex >= 0) {
                            //check for extra filename field
                            int fileNameStart = header.indexOf(FILENAME_LOOKUP);
                            if (fileNameStart >= 0) {
                                String filename = header.substring(fileNameStart + FILENAME_LOOKUP.length(), header.indexOf(DELIMITER_CARRIAGE_RETURN_AND_NEW_LINE, fileNameStart));
                                p.filename = filename.replace('"', ' ').replace('\'', ' ').trim();
                            }
                        } else {
                            // skip entry if no name is found
                            continue;
                        }
                        p.bytes = Arrays.copyOfRange(part, headerEnd + 4, part.length);
                        list.add(p);
                    }
                }
                handle(httpExchange, list);
            } else {
                //if no form data is present, still call handle method
                handle(httpExchange, null);
            }
        } else {
            textResponse(httpExchange, OK, STATUS_UP);
        }
    }

    private void handle(HttpExchange httpExchange, List<MultiPart> parts) {
        HtmlToPdfUtils.PrinterOptions po = new HtmlToPdfUtils.PrinterOptions();
        String url = httpExchange.getRequestURI().toString();
        po.printoutSettings(url);
        createDirectory(po.getWorkdir());
        Path file;
        for (MultiPart part : parts) {
            file = po.getWorkdir().resolve(part.filename);
            try {
                Files.write(file, part.bytes);
            } catch (IOException e) {
                textResponse(httpExchange, INTERNAL_SERVER_ERROR, "Error saving multipart");
            }
        }
        Path indexHtml = po.getWorkdir().resolve(INDEX_HTML);
        if (indexHtml.toFile().exists() && indexHtml.toFile().canRead()) {
            po.htmlToPdf();
            Path resultPdf = po.getWorkdir().resolve(RESULT_PDF);
            if (resultPdf.toFile().exists() && resultPdf.toFile().isFile()) {
                try (InputStream inputStream = Files.newInputStream(resultPdf); OutputStream outputStream = httpExchange.getResponseBody()){
                    httpExchange.getResponseHeaders().add(CONTENT_TYPE, APPLICATION_PDF);
                    httpExchange.getResponseHeaders().add(CONTENT_DISPOSITION, PDF_ATTACHED);
                    httpExchange.sendResponseHeaders(OK, resultPdf.toFile().length());
                    inputStream.transferTo(outputStream);
                    outputStream.flush();
                    httpExchange.getRequestBody().close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Error sending " + RESULT_PDF, e);
                    textResponse(httpExchange, INTERNAL_SERVER_ERROR, "Error sending " + RESULT_PDF);
                }
            } else {
                textResponse(httpExchange, INTERNAL_SERVER_ERROR, "No " + RESULT_PDF);
            }
        } else {
            textResponse(httpExchange, INTERNAL_SERVER_ERROR, "No " + INDEX_HTML);
        }
        deleteFilesAndDirectories(po.getWorkdir());
    }

    private static byte[] requestBody(HttpExchange httpExchange) {
        try (InputStream requestStream = httpExchange.getRequestBody(); ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            requestStream.transferTo(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Integer> indexesOf(byte[] array, byte[] pattern) {
        List<Integer> result = new ArrayList<>(0);
        for (int i = 0; i < array.length - pattern.length + 1; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (array[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                result.add(i);
            }
        }
        return result;
    }

    private static class MultiPart {
        private String filename;
        private byte[] bytes;
    }

    private static void textResponse(HttpExchange exchange, int rCode, String msg) {
        try (OutputStream os = exchange.getResponseBody()){
            exchange.getResponseHeaders().add(CONTENT_TYPE, TEXT_PLAIN);
            exchange.sendResponseHeaders(rCode, msg.length());
            os.write(msg.getBytes(DEFAULT_CHARSET));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error sending plain text response", e);
        } finally {
            exchange.close();
        }
    }
}
