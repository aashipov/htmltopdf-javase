package org.dummy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    /**
     * HTML to PDF.
     * @param printerOptions {@link PrinterOptions}
     */
    public static void htmlToPdf(PrinterOptions printerOptions) {
        executeAsync(printerOptions.getWrapper());
        if (!printerOptions.getWrapper().isOK()) {
            LOG.info(printerOptions.getWrapper().getOutputString() + DELIMITER_NEW_LINE + printerOptions.getWrapper().getErrorString());
        }
    }

    /**
     * Printer options.
     */
    public static class PrinterOptions {

        private static final String TMP = "tmp";
        private static final String DEFAULT_MARGIN = "20";
        private static final String MANY_SYMBOLS = ".*";
        private static final String A_3_PAPER_SIZE_NAME = MANY_SYMBOLS + "a3" + MANY_SYMBOLS;
        private static final String LANDSCAPE_REGEX = MANY_SYMBOLS + "landscape" + MANY_SYMBOLS;
        private static final String LEFT_PARENTHESIS = "(";
        private static final String RIGHT_PARENTHESIS = ")";
        private static final String ONE_OR_MORE_DIGITS_REGEX = "\\d+";
        private static final String ONE_OR_MORE_DIGITS_GROUP = LEFT_PARENTHESIS + ONE_OR_MORE_DIGITS_REGEX + RIGHT_PARENTHESIS;
        private static final String LEFT_MARGIN_NAME = "left";
        private static final String RIGHT_MARGIN_NAME = "right";
        private static final String TOP_MARGIN_NAME = "top";
        private static final String BOTTOM_MARGIN_NAME = "bottom";
        private static final Map<String, String> MARGIN_NAME_TO_REGEX = fillMarginNameRegexMap();

        private PaperSize paperSize = PaperSize.A4;
        private boolean landscape = false;
        private String left = DEFAULT_MARGIN;
        private String right = DEFAULT_MARGIN;
        private String top  = DEFAULT_MARGIN;
        private String bottom = DEFAULT_MARGIN;
        private final Path workdir = Paths.get(".").resolve(TMP).resolve(getRandomUUID());
        private OsCommandWrapper wrapper;

        /**
         * Constructor.
         */
        public PrinterOptions() {
            //
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

        public PrinterOptions buildOsCommandWrapper() {
            this.wrapper = new OsUtils.OsCommandWrapper(buildWkhtmltopdfCmd(this));
            this.wrapper.setWorkdir(this.workdir).setMaxExecuteTime(MAX_EXECUTE_TIME);
            return this;
        }

        /**
         * Does string matches regex
         * @param regex regex
         * @param string string
         * @return matches?
         */
        private static boolean matches(String regex, String string) {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            return pattern.matcher(string).matches();
        }

        /**
         * Extract groups matching regex.
         * @param regex regex
         * @param string string
         * @return {@link List} {@link String} of groups matched
         */
        private static List<String> groups(String regex, String string) {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(string);
            List<String> result = new ArrayList<>(0);
            while (matcher.find()) {
                result.add(matcher.group());
            }
            return result;
        }

        /**
         * Extract paper size and margins from URL.
         * @param url request URL
         */
        @SuppressWarnings("java:S3776")
        public void printoutSettings(String url) {
            if (isNotEmpty(url)) {
                if (matches(A_3_PAPER_SIZE_NAME, url)) {
                    this.setPaperSize(PaperSize.A3);
                }
                if (matches(LANDSCAPE_REGEX, url)) {
                    this.setLandscape(true);
                }
                String marginNameWithDigits;
                String marginDigits;
                String marginName;
                String marginRegex;
                List<String> found;
                for (Map.Entry<String, String> entry : MARGIN_NAME_TO_REGEX.entrySet()) {
                    marginName = entry.getKey();
                    marginRegex = entry.getValue();
                    found = groups(marginRegex, url);
                    if (isNotEmpty(found)) {
                        marginNameWithDigits = found.get(0);
                        found = groups(ONE_OR_MORE_DIGITS_GROUP, marginNameWithDigits);
                        if (isNotEmpty(found)) {
                            marginDigits = found.get(0);
                            if (isNotEmpty(marginDigits)) {
                                if (LEFT_MARGIN_NAME.equals(marginName)) {
                                    this.setLeft(marginDigits);
                                }
                                if (RIGHT_MARGIN_NAME.equals(marginName)) {
                                    this.setRight(marginDigits);
                                }
                                if (TOP_MARGIN_NAME.equals(marginName)) {
                                    this.setTop(marginDigits);
                                }
                                if (BOTTOM_MARGIN_NAME.equals(marginName)) {
                                    this.setBottom(marginDigits);
                                }
                            }
                        }
                    }
                }
            }
        }

        private static Map<String, String> fillMarginNameRegexMap() {
            Map<String, String> map = new HashMap<>();
            map.put(LEFT_MARGIN_NAME, LEFT_PARENTHESIS + LEFT_MARGIN_NAME + RIGHT_PARENTHESIS + ONE_OR_MORE_DIGITS_REGEX);
            map.put(RIGHT_MARGIN_NAME, LEFT_PARENTHESIS + RIGHT_MARGIN_NAME + RIGHT_PARENTHESIS + ONE_OR_MORE_DIGITS_GROUP);
            map.put(TOP_MARGIN_NAME, LEFT_PARENTHESIS + TOP_MARGIN_NAME + RIGHT_PARENTHESIS + ONE_OR_MORE_DIGITS_GROUP);
            map.put(BOTTOM_MARGIN_NAME, LEFT_PARENTHESIS + BOTTOM_MARGIN_NAME + RIGHT_PARENTHESIS + ONE_OR_MORE_DIGITS_GROUP);
            return map;
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
