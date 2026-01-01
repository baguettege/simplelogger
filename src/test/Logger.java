package test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Logger {
    private final String prefix;

    public Logger(String prefix) {
        this.prefix = prefix;
    }

    public Logger() {
        this(null);
    }

    private void log(Level level, String log, Throwable throwable) {
        StringBuilder indentBuilder = new StringBuilder();
        StringBuilder logBuilder = new StringBuilder();

        String time = Util.timeNow();
        String levelPrefix = level.name();
        String prefix = this.prefix;

        indentBuilder.append("[")
                .append(time)
                .append("] [")
                .append(levelPrefix)
                .append("]");

        if (prefix != null) {
            indentBuilder.append(" [")
                    .append(prefix)
                    .append("]");
        }

        logBuilder.append(log);

        if (throwable != null) {
            String stackTrace = Util.captureStackTrace(throwable);
            logBuilder.append("\n")
                    .append(stackTrace);
        }

        String finalIndent = indentBuilder.toString();
        String finalLog = logBuilder.toString();

        String formatted = Util.format(finalLog, finalIndent);

        if (level == Level.ERROR) {
            System.err.println(formatted);
            System.err.flush();
        } else {
            System.out.println(formatted);
        }
    }

    public void info(String log) {
        this.log(Level.INFO, log, null);
    }

    public void warn(String log) {
        this.log(Level.WARN, log, null);
    }

    public void error(String log) {
        this.log(Level.ERROR, log, null);
    }

    public void error(String log, Throwable throwable) {
        this.log(Level.ERROR, log, throwable);
    }

    public void debug(String log) {
        this.log(Level.DEBUG, log, null);
    }

    public void debug(String log, Throwable throwable) {
        this.log(Level.DEBUG, log, throwable);
    }

    private enum Level {
        INFO,
        WARN,
        ERROR,
        DEBUG
    }

    private static final class Util {
        private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        private static String timeNow() {
            return LocalTime.now().format(timeFormatter);
        }

        private static String captureStackTrace(Throwable throwable) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        }

        private static String format(String log, String indent) {
            if (log.isEmpty()) return indent;
            if (indent.isEmpty()) return log;

            StringBuilder logBuilder = new StringBuilder();

            String[] lines = log.split("\n");
            if (lines.length < 2)
                return indent + " " + log;

            int indentLen = indent.length();
            int numLines = lines.length;
            String fillerIndent = " ".repeat(indentLen);

            logBuilder.append(indent)
                    .append(" ")
                    .append(lines[0]);

            for (int i = 1; i < numLines; i++)
                logBuilder.append("\n")
                        .append(fillerIndent)
                        .append(" ")
                        .append(lines[i]);

            return logBuilder.toString();
        }
    }
}
