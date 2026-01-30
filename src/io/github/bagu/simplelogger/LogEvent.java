package io.github.bagu.simplelogger;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single log event with all associated metadata.
 * <p>
 * This is an immutable data class that encapsulates all information about a log message,
 * including when it occurred, its severity, which thread and logger produced it, the message
 * itself, and any parameters.
 *
 * @see Logger
 * @see LogSink
 */
public final class LogEvent {
    private final Instant timestamp;
    private final Level level;
    private final String threadName;
    private final String loggerName;
    private final String message;
    private final List<Object> params;

    /**
     * Constructs a log event with the specified properties.
     *
     * @param timestamp when the log event occurred
     * @param level the severity level of the event
     * @param threadName the name of the thread that generated the event
     * @param loggerName the name of the logger that generated the event
     * @param message the log message
     * @param params parameters for the message
     * @throws NullPointerException if any parameter is null
     */
    public LogEvent(
            Instant timestamp,
            Level level,
            String threadName,
            String loggerName,
            String message,
            List<Object> params
    ) {
        this.timestamp = Objects.requireNonNull(timestamp);
        this.level = Objects.requireNonNull(level);
        this.threadName = Objects.requireNonNull(threadName);
        this.loggerName = Objects.requireNonNull(loggerName);
        this.message = Objects.requireNonNull(message);
        this.params = List.copyOf(params);
    }

    /**
     * Returns the timestamp when this log event occurred.
     *
     * @return the timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns the severity level of this log event.
     *
     * @return the level
     */
    public Level level() {
        return level;
    }

    /**
     * Returns the name of the thread that generated this log event.
     *
     * @return the thread name
     */
    public String threadName() {
        return threadName;
    }

    /**
     * Returns the name of the logger that generated this log event.
     *
     * @return the logger name
     */
    public String loggerName() {
        return loggerName;
    }

    /**
     * Returns the message for this log event.
     *
     * @return the message
     */
    public String message() {
        return message;
    }

    /**
     * Returns the parameters for this log event.
     *
     * @return an immutable list of parameters
     */
    public List<Object> params() {
        return params;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "timestamp=" + timestamp +
                ", level=" + level +
                ", threadName='" + threadName + '\'' +
                ", loggerName='" + loggerName + '\'' +
                ", message='" + message + '\'' +
                ", params=" + params +
                '}';
    }
}
