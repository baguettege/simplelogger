package api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

/**
 * Utility class for {@link AbstractLogger}. Provides helper methods for formatting logs.
 */

public final class Util {
    private Util() {}

    /**
     * Captures the stack trace of a given {@link Throwable} and returns the result.
     *
     * @param throwable {@link Throwable} to capture the stack trace of
     * @return stack trace of the given {@link Throwable}
     */
    private static String captureStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * Returns an ANSI color code for a specified {@link LogSeverity}.
     *
     * @param severity log severity
     * @return ANSI escape sequence for the color of the given {@link LogSeverity}
     */
    private static String ansiCodeFor(LogSeverity severity) {
        return switch (severity) {
            case INFO -> AnsiConstants.NO_COLOR;
            case WARN -> AnsiConstants.YELLOW;
            case ERROR -> AnsiConstants.RED;
            case FATAL -> AnsiConstants.PURPLE;
            case TRACE -> AnsiConstants.WHITE;
            case DEBUG -> AnsiConstants.BLUE;
        };
    }

    /**
     * Formats a given log message with an indent, to align newlines with the indent by using
     * empty spaces, to allow proper separation of multi-lined logs.
     *
     * <p>
     *     Adds optional ANSI color output.
     * </p>
     *
     * @param log log message
     * @param indent log indent
     * @param ansiEnabled whether to add ANSI color output or not
     * @return formatted log
     */
    private static String formatIndent(String log, String indent, boolean ansiEnabled) {
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

    /**
     * Formats a given {@link LogEvent} into a single {@link String} representation and returns the result.
     * The result can be directly outputted to the console.
     *
     * @param logEvent event to format
     * @return formatted {@link LogEvent}, ready to output
     */
    static String formatEvent(LogEvent logEvent) {
        StringBuilder indentBuilder = new StringBuilder();
        StringBuilder logBuilder = new StringBuilder();

        LogPresentation logPresentation = logEvent.logPresentation();
        boolean ansiEnabled = logPresentation.ansiEnabled();

        Instant instant = logEvent.instant();
        String time = logPresentation.timeFormatter().format(instant);

        LogSeverity severity = logEvent.severity();

        indentBuilder.append("[")
                .append(time)
                .append("] [")
                .append(ansiEnabled ? Util.ansiCodeFor(severity) : "")
                .append(severity.name())
                .append(ansiEnabled ? AnsiConstants.RESET : "")
                .append("]");

        String prefix = logEvent.prefix();
        if (prefix != null) {
            indentBuilder.append(" [")
                    .append(prefix)
                    .append("]");
        }

        logBuilder.append(logEvent.log());

        Throwable throwable = logEvent.throwable();
        if (throwable != null) {
            String stackTrace = Util.captureStackTrace(throwable);
            logBuilder.append("\n")
                    .append(stackTrace);
        }

        String indent = indentBuilder.toString();
        String log = logBuilder.toString();

        return Util.formatIndent(log, indent, ansiEnabled);
    }
}
