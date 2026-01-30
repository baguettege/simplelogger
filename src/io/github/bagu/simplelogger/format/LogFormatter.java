package io.github.bagu.simplelogger.format;

import io.github.bagu.simplelogger.LogEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Formats log events into human-readable strings.
 * <p>
 * This formatter creates structured log output with a configurable prefix containing
 * metadata fields (timestamp, level, thread, logger) followed by the message.
 * <p>
 * Message formatting supports:
 * <ul>
 * <li>Parameter substitution using {@code {}} placeholders</li>
 * <li>Escaping with {@code \{}} for literal braces</li>
 * <li>Automatic stack trace appending, assuming the final parameter given is a {@link Throwable}</li>
 * <li>Multi-line message indentation</li>
 * </ul>
 * <p>
 * Example output:
 * <pre>{@code
 * [2024-01-28 14:30:45] [INFO] [main] [MyApp] Application started
 * [2024-01-28 14:30:46] [ERROR] [worker-1] [MyApp] Failed to connect: Connection refused
 *                                                   java.net.ConnectException: Connection refused
 *                                                       at ...
 * }</pre>
 *
 * @see LogEvent
 * @see LogField
 * @see TimeFormatter
 */
public final class LogFormatter {
    private static final String NEW_LINE = System.lineSeparator();

    private final TimeFormatter timeFormatter;
    private final List<LogField> fields;

    /**
     * Constructs a log formatter with the specified time formatter and fields.
     * <p>
     * Fields indicate the prefix applied to each log, before the message. For example, given
     * {@link LogField#TIMESTAMP}, {@link LogField#LEVEL}, the output will be
     * {@code [2024-01-28 14:30:45] [INFO] Application started}
     *
     * @param timeFormatter the formatter to use for rendering timestamps
     * @param fields the fields to include in the log prefix, in order
     * @throws NullPointerException if any parameter is null
     */
    public LogFormatter(TimeFormatter  timeFormatter, LogField... fields) {
        this.timeFormatter = Objects.requireNonNull(timeFormatter);
        Objects.requireNonNull(fields);
        for (LogField field : fields) {
            Objects.requireNonNull(field);
        }
        this.fields = List.of(fields);
    }

    /**
     * Formats a log event into a human-readable string.
     * <p>
     * The output consists of a prefix (built from the configured fields) followed
     * by the formatted message. Multi-line messages are indented to align with the prefix.
     *
     * @param event the event to format
     * @return the formatted log string
     * @throws NullPointerException if the event is null
     */
    public String format(LogEvent event) {
        Objects.requireNonNull(event);

        String prefix = buildPrefix(event);
        String message = substituteParams(event.message(), event.params());

        return applyIndent(prefix, message);
    }

    /**
     * Builds the log prefix from the configured fields.
     * <p>
     * Each field is rendered in square brackets, separated by spaces.
     *
     * @param event the event to extract field values from
     * @return the formatted prefix string
     */
    private String buildPrefix(LogEvent event) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fields.size(); i++) {
            LogField field = fields.get(i);

            switch (field) {
                case TIMESTAMP -> {
                    String timestamp = timeFormatter.format(event.timestamp());
                    sb.append('[').append(timestamp).append(']');
                }
                case LEVEL -> {
                    String level = event.level().toString();
                    sb.append('[').append(level).append(']');
                }
                case THREAD_NAME -> {
                    String threadName = event.threadName();
                    sb.append('[').append(threadName).append(']');
                }
                case LOGGER_NAME -> {
                    String loggerName = event.loggerName();
                    sb.append('[').append(loggerName).append(']');
                }
            }

            if (i != fields.size() - 1) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "LogFormatter{" +
                "timeFormatter=" + timeFormatter +
                ", fields=" + fields +
                '}';
    }

    /**
     * Substitutes parameter values into a message template, with exception formatting.
     * <p>
     * Substitution rules:
     * <ul>
     * <li>{@code {}} - substituted with the next parameter</li>
     * <li>{@code \{}} - literal {@code {}}</li>
     * <li>{@code \\{}} - literal {@code \} followed by parameter substitution</li>
     * </ul>
     * <p>
     * If the last parameter is a {@link Throwable}, its stack trace is appended to the end of the message.
     *
     * @param message the message template
     * @param params the parameters to substitute
     * @return the message string with substituted parameters
     */
    private static String substituteParams(String message, List<Object> params) {
        // last param if throwable prints stack trace
        // {} = substitute param
        // \{} = literal "{}"
        // \\{} = '\' + substitute param

        if (params.isEmpty()) {
            return message;
        }

        StringBuilder sb = new StringBuilder();
        Throwable throwable = null;

        if (params.get(params.size() - 1) instanceof Throwable t) {
            throwable = t;
            params = params.subList(0, params.size() - 1);
        }

        if (!params.isEmpty()) {
            Iterator<Object> it = params.iterator();

            for (int i = 0; i < message.length(); i++) {
                char c = message.charAt(i);

                if (c == '\\' && i + 1 < message.length()) {
                    // escaping
                    char next = message.charAt(i + 1);

                    if (next == '{' &&
                            i + 2 < message.length() &&
                            message.charAt(i + 2) == '}') {
                        // literal {}
                        sb.append("{}");
                        i += 2;
                    } else if (next == '\\') {
                        if (i + 3 < message.length() &&
                                message.charAt(i + 2) == '{' &&
                                message.charAt(i + 3) == '}') {
                            // literal \ + substitute param
                            sb.append('\\');
                            if (it.hasNext()) {
                                // param exists
                                sb.append(it.next());
                            } else {
                                // param not exists, leave literal {}
                                sb.append("{}");
                            }
                            i += 3;
                        } else {
                            // literal \
                            sb.append('\\');
                            i++;
                        }
                    } else {
                        // unknown escape, just do literal \ + next
                        sb.append('\\').append(next);
                        i++;
                    }
                } else if (c == '{' && i + 1 < message.length() && message.charAt(i + 1) == '}') {
                    // substitute {} with param
                    if (it.hasNext()) {
                        // param exists
                        sb.append(it.next());
                    } else {
                        // param not exists, leave literal {}
                        sb.append("{}");
                    }
                    i++;
                } else {
                    // normal char
                    sb.append(c);
                }
            }
        } else {
            sb.append(message);
        }

        // append throwable if exists
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            sb.append(NEW_LINE).append(sw);
        }

        return sb.toString();
    }

    /**
     * Applies indentation to multi-lined messages to align with the prefix.
     * <p>
     * The first line is placed immediately after the prefix. Subsequent lines are
     * indented to align vertically with the first line.
     *
     * @param prefix the log prefix
     * @param message the log message
     * @return the completed log string with proper indentation
     */
    private static String applyIndent(String prefix, String message) {
        if (prefix.isEmpty()) {
            return message;
        } else if (message.isEmpty()) {
            return prefix;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(" ");

        String indent = " ".repeat(prefix.length());

        Iterator<String> it = message.lines().iterator();
        sb.append(it.next());
        it.forEachRemaining(line -> {
            sb.append(NEW_LINE)
                    .append(indent)
                    .append(" ")
                    .append(line);
        });

        return sb.toString();
    }
}
