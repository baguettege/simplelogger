package api;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility class for {@link SimpleLogger} instances.
 */

public final class Util {
    private Util() {}

    /**
     * Captures the stack trace of a {@link Throwable} and returns the result.
     *
     * @param throwable the {@link Throwable} to capture
     * @return the stack trace of the {@link Throwable}
     */
    static String captureStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Returns the ANSI escape code color for a specified {@link SeverityLevel} of log.
     *
     * @param severity {@link SeverityLevel} of the log
     * @return the ANSI escape code color
     */
    static String colorFor(SeverityLevel severity) {
        return switch(severity) {
            case INFO -> AnsiConstants.NO_COLOR;
            case WARN -> AnsiConstants.YELLOW;
            case ERROR -> AnsiConstants.RED;
            case FATAL -> AnsiConstants.PURPLE;
            case TRACE -> AnsiConstants.WHITE;
            case DEBUG -> AnsiConstants.BLUE;
        };
    }

    /**
     * Builds the indent string for a log message.
     *
     * @param color ANSI color code for severity ({@code ""} if disabled)
     * @param ansiEnabled if ansi escape is enabled or not
     * @param time the formatted timestamp
     * @param severity the severity name
     * @param globalPrefixes the global prefixes for the {@link SimpleLogger}
     * @param additionalPrefixes optional, additional prefixes to append to the indent
     * @return formatted indent string
     */
    static String buildIndent(
            String color,
            boolean ansiEnabled,
            String time,
            String severity,
            String[] globalPrefixes,
            String[] additionalPrefixes
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("[")
                .append(time)
                .append("] [")
                .append(color)
                .append(severity)
                .append(ansiEnabled ? AnsiConstants.RESET : "")
                .append("]");

        for (String prefix : globalPrefixes)
            sb.append(" [").append(prefix).append("]");

        for (String prefix : additionalPrefixes)
            sb.append(" [").append(prefix).append("]");

        return sb.toString();
    }

    /**
     * Formats a log message with its indent (timestamp, prefixes, severity), allowing for multiple lined
     * messages to be clearly indented, creating clear separation between multi-lined logs.
     * <p>
     *     Example:
     *     <pre>
     *         {@code [16:28:33] [INFO] Connected to server at 127.0.0.1
     * [16:28:33] [FATAL] Program crashed
     *                    java.lang.OutOfMemoryError: Ran out of memory
     *                    	at test.Main.main(Main.java:13)
     * [16:28:33] [INFO] Program shut down}
     *     </pre>
     * </p>
     *
     * @param message the message to log
     * @param indent the indent for the log
     * @return formatted log string
     */
    static String format(String message, String indent) {
        String[] parts = message.split("\n");
        if (parts.length < 2) return indent + " " + message;

        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(" ").append(parts[0]);

        String blankIndent = " ".repeat(indent.replaceAll(AnsiConstants.REGEX, "").length() + 1);

        for (int i = 1; i < parts.length; i++) {
            sb.append("\n")
                    .append(blankIndent)
                    .append(parts[i]);
        }

        return sb.toString();
    }
}
