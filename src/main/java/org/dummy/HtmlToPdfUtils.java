package org.dummy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.logging.Logger;
import static org.dummy.EmptinessUtils.isNotEmpty;
import static org.dummy.OsUtils.*;

/**
 * HTML to PDF via wkhtmltopdf(.exe).
 */
public final class HtmlToPdfUtils {

    private static final Logger LOG = Logger.getLogger(HtmlToPdfUtils.class.getSimpleName());
    private static final int MAX_EXECUTE_TIME = 600_000;
    private static final String WKHTMLTOPDF_EXECUTABLE = isLinux() ? "wkhtmltopdf" : "wkhtmltopdf.exe";
    private static final String INDEX_HTML = "index.html";
    public static final String RESULT_PDF = "result.pdf";

    /**
     * Constructor.
     */
    private HtmlToPdfUtils() {
        //
    }

    private static String buildWkhtmltopdfCmd(PrinterOptions printerOptions) {
        StringJoiner sj = new StringJoiner(DELIMITER_SPACE);
        sj.add(WKHTMLTOPDF_EXECUTABLE);
        sj.add("--enable-local-file-access");
        sj.add("--print-media-type");
        sj.add("--no-stop-slow-scripts");
        sj.add("--disable-smart-shrinking");

        if (isNotEmpty(printerOptions.getLeft())) {
            sj.add("--margin-left");
            sj.add(printerOptions.getLeft());
        }
        if (isNotEmpty(printerOptions.getRight())) {
            sj.add("--margin-right");
            sj.add(printerOptions.getRight());
        }
        if (isNotEmpty(printerOptions.getTop())) {
            sj.add("--margin-top");
            sj.add(printerOptions.getTop());
        }
        if (isNotEmpty(printerOptions.getBottom())) {
            sj.add("--margin-bottom");
            sj.add(printerOptions.getBottom());
        }

        if (isNotEmpty(printerOptions.getPaperSize())) {
            if (isNotEmpty(printerOptions.getPaperSize().getWidth())) {
                sj.add("--page-width");
                sj.add(printerOptions.getPaperSize().getWidth());
            }
            if (isNotEmpty(printerOptions.getPaperSize().getHeight())) {
                sj.add("--page-height");
                sj.add(printerOptions.getPaperSize().getHeight());
            }
        }

        if (printerOptions.isLandscape()) {
            sj.add("--orientation");
            sj.add("landscape");
        }
        sj.add(INDEX_HTML);
        sj.add(RESULT_PDF);
        return sj.toString();
    }

    /**
     * HTML to PDF.
     * @param printerOptions {@link PrinterOptions}
     */
    public static void htmlToPdf(PrinterOptions printerOptions) {
        executeAsync(printerOptions.getWrapper());
        if (!printerOptions.getWrapper().isOK()) {
            LOG.info(printerOptions.getWrapper().getOutputString() + "\n" + printerOptions.getWrapper().getErrorString());
        }
    }

    /**
     * Printer options.
     */
    public static class PrinterOptions {

        private PaperSize paperSize = PaperSize.A4;
        private boolean landscape = false;
        private String left = EMPTY_STRING;
        private String right = EMPTY_STRING;
        private String top  = EMPTY_STRING;
        private String bottom = EMPTY_STRING;
        private final Path workdir;
        private final OsCommandWrapper wrapper;

        /**
         * Constructor.
         */
        public PrinterOptions() {
            this.workdir = Paths.get(".").resolve(getRandomUUID());
            this.wrapper = new OsUtils.OsCommandWrapper(buildWkhtmltopdfCmd(this));
            this.wrapper.setWorkdir(this.workdir).setMaxExecuteTime(MAX_EXECUTE_TIME);
        }

        public PaperSize getPaperSize() {
            return paperSize;
        }

        public PrinterOptions setPaperSize(PaperSize paperSize) {
            this.paperSize = paperSize;
            return this;
        }

        public boolean isLandscape() {
            return landscape;
        }

        public PrinterOptions setLandscape(boolean landscape) {
            this.landscape = landscape;
            return this;
        }

        public String getLeft() {
            return left;
        }

        public PrinterOptions setLeft(String left) {
            this.left = left;
            return this;
        }

        public String getRight() {
            return right;
        }

        public PrinterOptions setRight(String right) {
            this.right = right;
            return this;
        }

        public String getTop() {
            return top;
        }

        public PrinterOptions setTop(String top) {
            this.top = top;
            return this;
        }

        public String getBottom() {
            return bottom;
        }

        public PrinterOptions setBottom(String bottom) {
            this.bottom = bottom;
            return this;
        }

        public Path getWorkdir() {
            return workdir;
        }

        public OsCommandWrapper getWrapper() {
            return wrapper;
        }

        /**
         * A4 with margins.
         * @param l left
         * @param r right
         * @param t top
         * @param b bottom
         * @return {@link PrinterOptions}
         */
        public static PrinterOptions withMargins(String l, String r, String t, String b) {
            return (new PrinterOptions()).setLeft(l).setRight(r).setTop(t).setBottom(b);
        }
    }

    /**
     * Office paper size.
     */
    private enum PaperSize {
        A4("210", "297"),
        A3("297", "420")
        ;

        private final String width;
        private final String height;

        /**
         * Constructor.
         * @param width width
         * @param height height
         */
        PaperSize(String width, String height) {
            this.width = width;
            this.height = height;
        }

        public String getWidth() {
            return width;
        }

        public String getHeight() {
            return height;
        }
    }
}
