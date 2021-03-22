package org.dummy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.dummy.EmptinessUtils.isBlank;
import static org.dummy.OsUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты {@link OsUtils}.
 */
class OsUtilsTest {

    private static final String JAVA_VERSION_CMD = "java -version";
    private static final String JAVA_VERSION_EXPECTED_OUT_MSG = "Runtime Environment";

    private static String get30SecondsSleepCmdText() {
        return isWindows() ? "ping -n 30 127.0.0.1" : "sleep 30000";
    }

    @Test
    void executeJavaVersionTest() {
        OsUtils.OsCommandWrapper result = execute(JAVA_VERSION_CMD);
        assertTrue(result.isOK(), "java -version runs smoothly");
        assertTrue(result.getErrorString().contains(JAVA_VERSION_EXPECTED_OUT_MSG), "java -version prints to stderr");
        assertTrue(result.getOutput().isEmpty(), "Log is empty");
    }

    @Test
    void executeUpdateJavaVersionTest() {
        OsUtils.OsCommandWrapper result = executeAsync(JAVA_VERSION_CMD);
        assertTrue(result.isOK(), "java -version won't crash executeUpdate");
        assertTrue(result.getErrorString().contains(JAVA_VERSION_EXPECTED_OUT_MSG), "java -version prints to stderr");
        assertTrue(isBlank(result.getOutputString()), "Log is empty");
    }

    @Test
    void executeAsyncTimeoutTest() {
        String cmd = get30SecondsSleepCmdText();
        OsUtils.OsCommandWrapper wrapper = executeAsync(cmd, Integer.parseInt("1000"));
        assertFalse(wrapper.isOK(), "Error");
        assertTrue(wrapper.hasPid(), "Process had PID");
        assertTrue(wrapper.getErrorString().contains("Timeout"), "Timed out");
        if (isWindows()) {
            assertTrue(wrapper.getOutputString().contains("Pinging"), "Pinging");
        } else {
            assertTrue(wrapper.getOutput().isEmpty(), "Empty output");
        }
    }

    @Test
    void executeAsyncTest() {
        OsUtils.OsCommandWrapper wrapper = executeAsync(JAVA_VERSION_CMD);
        assertTrue(wrapper.isOK(), "java -version runs smoothly");
        assertTrue(wrapper.getErrorString().contains(JAVA_VERSION_EXPECTED_OUT_MSG), "java -version prints to stderr");
        assertTrue(wrapper.getOutput().isEmpty(), "Log is empty");
    }

    @Test
    void executeInWorkDirTest() {
        if (isWindows()) {
            OsUtils.OsCommandWrapper wrapper = new OsUtils.OsCommandWrapper("ls");
            Path workdir = Paths.get(System.getenv("HOMEDRIVE")).resolve(System.getenv("HOMEPATH"));
            wrapper.setWorkdir(workdir);
            execute(wrapper);
            assertTrue(wrapper.getOutput().contains("NTUSER.DAT"), "NTUSER.DAT");
        }
    }

    @Test
    public void isProcessAliveTest(){
        List<String> javaPids = getProcessIdByProcessName("java");
        for (String pid : javaPids) {
            assertTrue(isProcessAlive(pid), "Every instance of java is running");
        }
    }

    @Disabled("proof of concept")
    @Test
    void whyOsUtils() throws IOException, InterruptedException {
        final long timeout = 10L;
        ProcessBuilder processBuilder = new ProcessBuilder().command(translateCommandline(get30SecondsSleepCmdText()));
        Process process = processBuilder.start();
        if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
            assertTrue(process.pid() > 0L, "some pid");
            assertTrue(isProcessAlive("" + process.pid()), "alive");
            assertTrue(process.isAlive());
            process.destroy();
        }
        while (process.isAlive()) {
            TimeUnit.MILLISECONDS.sleep(timeout);
        }
        assertFalse(isProcessAlive("" + process.pid()), "stopped");
        if (isWindows()) {
            assertEquals(1, process.exitValue(), "exit code 1");
            String err = inputStreamToStringJdk9Plus(process.getErrorStream(), getConsoleCodepage());
            String out = inputStreamToStringJdk9Plus(process.getInputStream(), getConsoleCodepage());
            assertTrue(isBlank(err), "no error");
            assertTrue(out.contains("Reply from 127.0.0.1: "));
        }
        if (isLinux()) {
            assertEquals(143, process.exitValue(), "SIGTERM");
            Assertions.assertThrows(
                    IOException.class,
                    () -> {
                        inputStreamToStringJdk9Plus(process.getInputStream(), getConsoleCodepage());
                    },
                    "Stream closed");
            Assertions.assertThrows(
                    IOException.class,
                    () -> {
                        inputStreamToStringJdk9Plus(process.getErrorStream(), getConsoleCodepage());
                    },
                    "Stream closed");
        }
    }

    /**
     * Use JDK 9+ {@link InputStream#transferTo(OutputStream)} to get a {@link String} out of {@link InputStream}.
     * @param inputStream {@link InputStream}
     * @param charset {@link Charset}
     * @return {@link String} в {@link Charset}
     * @throws IOException copy
     * Will not {@link InputStream#close()}
     */
    private static String inputStreamToStringJdk9Plus(InputStream inputStream, Charset charset) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toString(charset);
        }
    }
}
