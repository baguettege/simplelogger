package io.github.bagu.simplelogger.format;

import io.github.bagu.simplelogger.LogEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

/**
 * The default log formatter that produces multi-line formatted log output with timestamps, levels,
 * thread names (optional), logger names and formatted messages.
 *
 * <p>
 *       Output format:
 *      <pre>
 *      [timestamp] [level] [thread] [logger] message
 *                                            continued lines are indented
 *      </pre>
 * </p>
 *
 * <p>
 *     This formatter supports:<br>
 *     - parameter substitution using {@code {}} placeholders<br>
 *     - Escape sequences: {@code \\} for backslash, {@code \{}} for literal brace<br>
 *     - Automatic exception stack trace formatting (if last parameter is {@link Throwable})<br>
 *     - Multi-line message indentation
 * </p>
 *
 * <p>
 *     This class is immutable.
 * </p>
 *
 * <pre>{@code
 * LogFormatter formatter = new DefaultLogFormatter(
 *     new SimpleTimeFormatter(),
 *     true  // show thread names
 * );
 *
 * // Example output:
 * // [14:30:15] [INFO] [main] [MyApp] Application started on port 8080
 * }</pre>
 */
public final class DefaultLogFormatter implements LogFormatter {
    private final TimeFormatter timeFormatter;
    private final boolean showThreadName;

    /**
     * Creates a new formatter with the specified time formatter and thread name display option.
     *
     * @param timeFormatter the formatter for timestamps
     * @param showThreadName {@code true} to include thread names in output, {@code false} to omit them
     */
    public DefaultLogFormatter(TimeFormatter timeFormatter, boolean showThreadName) {
        this.timeFormatter = timeFormatter;
        this.showThreadName = showThreadName;
    }

    @Override
    public String format(LogEvent event) {
        String indent = buildIndent(event, timeFormatter, showThreadName);
        String message = formatMessage(event.message(), event.parameters());
        return formatIndentation(message, indent);
    }

    /**
     * Builds the indent for a log event.
     *
     * @param event the log event
     * @param timeFormatter the time formatter
     * @param showThreadName if the thread name should be included
     * @return formatted log indent
     */
    private static String buildIndent(
            LogEvent event,
            TimeFormatter timeFormatter,
            boolean showThreadName
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append(timeFormatter.format(event.epochMillis()))
                .append("] [")
                .append(event.level())
                .append("]");

        if (showThreadName)
            sb.append(" [")
                    .append(event.threadName())
                    .append("]");

        sb.append(" [")
                .append(event.loggerName())
                .append("]");

        return sb.toString();
    }

    /**
     * Substitutes parameters into a message template.
     *
     * <p>
     *     Rules:<br>
     *     - {@code {}} is replaced with the next parameter<br>
     *     - {@code \\} produces a literal backslash<br>
     *     - {@code \{}} produces a literal {@code {}}<br>
     *     - {@code \{} produces a literal opening brace<br>
     *     - If the last parameter is a {@link Throwable}, its stack track will be appended to the message instead.<br>
     *     Edge cases:<br>
     *     - More {@code {}} than parameters results in {@code {}} left as-is<br>
     *     - More parameters than {@code {}} results in extra parameters ignored.<br>
     *     - {@code \} at the end of string outputs {@code \}
     * </p>
     *
     * @param message the message template
     * @param parameters parameters to substitute into the message template
     * @return the final message
     */
    private static String formatMessage(String message, List<Object> parameters) {
        StringBuilder sb = new StringBuilder();
        Throwable throwable = null;

        // check if last obj is a throwable
        if (!parameters.isEmpty() &&
                parameters.get(parameters.size() - 1) instanceof Throwable) {
            Throwable t = (Throwable) parameters.get(parameters.size() - 1);
            parameters = parameters.subList(0, parameters.size() - 1); // remove last obj
            throwable = t;
        }

        Iterator<Object> paramIterator = parameters.iterator();

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            if (c == '\\' && i + 1 < message.length()) {
                char next = message.charAt(i + 1);

                if (next == '\\') {
                    sb.append('\\');
                    i++;
                } else if (next == '{') {
                    sb.append('{');
                    i++;
                } else {
                    sb.append('\\');
                }
            } else if (c == '{' && i + 1 < message.length() && message.charAt(i + 1) == '}') {
                if (paramIterator.hasNext()) {
                    Object obj = paramIterator.next();
                    sb.append(obj);
                } else {
                    sb.append("{}");
                }
                i++;
            } else {
                sb.append(c);
            }
        }

        if (throwable != null)
            sb.append("\n").append(captureStackTrace(throwable));

        return sb.toString();
    }

    /**
     * Indents multi-lined messages with a given indent.
     *
     * @param message the message
     * @param indent the indent
     * @return the final log to output with indented newlines
     */
    private static String formatIndentation(String message, String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent);
        String fillerIndent = " ".repeat(indent.length() + 1);

        Iterator<String> lineIterator = message.lines().iterator();
        if (lineIterator.hasNext())
            sb.append(" ")
                    .append(lineIterator.next());

        lineIterator.forEachRemaining(line ->
                sb.append("\n")
                        .append(fillerIndent)
                        .append(line)
                );

        return sb.toString();
    }

    /**
     * Captures and returns the stack trace of a given {@link Throwable}
     *
     * @param throwable the throwable
     * @return the stack trace of the throwable
     */
    private static String captureStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }
}
