package api;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Util {
    private Util() {}

    static String captureStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    static String formatIndent(String log, String indent, boolean ansiEnabled) {
        if (log.isEmpty()) return indent;
        if (indent.isEmpty()) return log;

        StringBuilder logBuilder = new StringBuilder();

        String[] lines = log.split("\n");
        if (lines.length < 2)
            return indent + " " + log;

        int indentLen = !ansiEnabled ? indent.length() : indent.replaceAll(AnsiConstants.REGEX, "").length();
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

    static String ansiCodeFor(LogSeverity severity) {
        return switch (severity) {
            case INFO -> AnsiConstants.NO_COLOR;
            case WARN -> AnsiConstants.YELLOW;
            case ERROR -> AnsiConstants.RED;
            case FATAL -> AnsiConstants.PURPLE;
            case TRACE -> AnsiConstants.WHITE;
            case DEBUG -> AnsiConstants.BLUE;
        };
    }
}
