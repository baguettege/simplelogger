package io.github.bagu.simplelogger;

import java.util.List;
import java.util.Objects;

/**
 * An immutable record of a logging event containing all contextual information.
 *
 * <p>
 *     A {@link LogEvent} captures the complete state of a log message at the time it was created,
 *     including timestamp, severity level, message template, object parameters, logger name, and thread
 *     name.
 * </p>
 *
 *
 * <p>
 *     The message and parameters follow a placeholder format where {@code {}} in the message is replaced with
 *     corresponding parameter values during formatting.
 * </p>
 *
 * <pre>
 *     {@code LogEvent event = new LogEvent(
 *         System.currentTimeMillis(),
 *         Level.INFO,
 *         "User {} logged in from {}",
 *         List.of("alice", "192.168.0.1"),
 *         "AuthService",
 *         Thread.currentThread().getName()
 * );}
 * </pre>
 */
public class LogEvent {
    private final long epochMillis;
    private final Level level;
    private final String message;
    private final List<Object> parameters;
    private final String loggerName;
    private final String threadName;

    /**
     * Creates a new immutable log event.
     *
     * @param epochMillis the timestamp in milliseconds since the Unix epoch
     * @param level the severity level
     * @param message the log message template with optional {@code {}} placeholders
     * @param parameters the list of parameters to substitute into the message
     * @param loggerName the name of the logger that created this event
     * @param threadName the name of the thread that created this event
     */
    public LogEvent(
            long epochMillis,
            Level level,
            String message,
            List<Object> parameters,
            String loggerName,
            String threadName
    ) {
        this.epochMillis = epochMillis;
        this.level = Objects.requireNonNull(level);
        this.message = Objects.requireNonNull(message);
        this.parameters = List.copyOf(parameters);
        this.loggerName = Objects.requireNonNull(loggerName);
        this.threadName = Objects.requireNonNull(threadName);
    }

    /**
     * Returns the timestamp when this event was created.
     *
     * @return milliseconds since the Unix epoch
     */
    public long epochMillis() {
        return epochMillis;
    }

    /**
     * Returns the severity level of this event.
     *
     * @return the log level
     */
    public Level level() {
        return level;
    }

    /**
     * Returns the message template with optional {@code {}} placeholders.
     *
     * @return the message template
     */
    public String message() {
        return message;
    }

    /**
     * Returns an immutable list of parameters for message substitution.
     *
     * @return the parameter list
     */
    public List<Object> parameters() {
        return parameters;
    }

    /**
     * Returns the name of the logger that created this event.
     *
     * @return the logger name
     */
    public String loggerName() {
        return loggerName;
    }

    /**
     * Returns the name of the thread that created this event.
     *
     * @return the thread name
     */
    public String threadName() {
        return threadName;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "epochMillis=" + epochMillis +
                ", level=" + level +
                ", message=" + message +
                ", parameters=" + parameters +
                ", loggerName=" + loggerName +
                ", threadName=" + threadName +
                "}";
    }
}
