package io.github.bagu.simplelogger;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * A logger that sends log events to a {@link LogSink}.
 * <p>
 * Logger instances are configured with a name, a sink to write events to, and an optional
 * minimum severity level. Log messages below the minimum level are silently dropped.
 * <p>
 * Thread safety: This class is thread-safe and can be used concurrently from multiple threads,
 * so long as the underlying log sink is thread-safe itself.
 *
 * @see LogSink
 * @see LogEvent
 * @see Level
 */
public final class Logger implements AutoCloseable {
    private final String name;
    private final LogSink sink;
    private final Level minLevel;

    /**
     * Constructs a logger with the specified name, sink, and minimum level.
     *
     * @param name the name of this logger
     * @param minLevel the minimum severity level
     * @param sink the sink to write log events to
     * @throws NullPointerException if any parameter is null
     */
    public Logger(String name, Level minLevel, LogSink sink) {
        this.name = Objects.requireNonNull(name);
        this.minLevel = Objects.requireNonNull(minLevel);
        this.sink = Objects.requireNonNull(sink);
    }

    /**
     * Constructs a logger with the specified name and sink.
     * <p>
     * The minimum level defaults to {@link Level#TRACE}, meaning all log events will be written.
     *
     * @param name the name of this logger
     * @param sink the sink to write log events to
     * @throws NullPointerException if any parameter is null
     */
    public Logger(String name, LogSink sink) {
        // log everything by default
        this(name, Level.TRACE, sink);
    }

    /**
     * Internal method to log a message at a specified level with optional parameters.
     * <p>
     * If the sink is closed or the level is below the minimum, the log is silently dropped.
     *
     * @param level the severity level of the log
     * @param message the message to log
     * @param params optional parameters
     * @throws NullPointerException if level or message is null
     */
    private void log(
            Level level,
            String message,
            Object... params
    ) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(message);

        if (sink.isClosed() || level.ordinal() < minLevel.ordinal()) {
            return;
        }

        sink.accept(new LogEvent(
                Instant.now(),
                level,
                Thread.currentThread().getName(),
                name,
                message,
                Arrays.asList(params)
        ));
    }

    /**
     * Logs a message at the {@link Level#TRACE} level.
     *
     * @param message the message to log
     * @param params optional parameters
     */
    public void trace(String message, Object... params) {
        this.log(Level.TRACE, message, params);
    }

    /**
     * Logs a message at the {@link Level#DEBUG} level.
     *
     * @param message the message to log
     * @param params optional parameters
     */
    public void debug(String message, Object... params) {
        this.log(Level.DEBUG, message, params);
    }

    /**
     * Logs a message at the {@link Level#INFO} level.
     *
     * @param message the message to log
     * @param params optional parameters
     */
    public void info(String message, Object... params) {
        this.log(Level.INFO, message, params);
    }

    /**
     * Logs a message at the {@link Level#WARN} level.
     *
     * @param message the message to log
     * @param params optional parameters
     */
    public void warn(String message, Object... params) {
        this.log(Level.WARN, message, params);
    }

    /**
     * Logs a message at the {@link Level#ERROR} level.
     *
     * @param message the message to log
     * @param params optional parameters
     */
    public void error(String message, Object... params) {
        this.log(Level.ERROR, message, params);
    }

    /**
     * Logs a message at the {@link Level#FATAL} level.
     *
     * @param message the message to log
     * @param params optional parameters
     */
    public void fatal(String message, Object... params) {
        this.log(Level.FATAL, message, params);
    }

    /**
     * Closes this logger and its underlying sink.
     * <p>
     * This method is synchronized and idempotent to ensure the sink is closed exactly once.
     */
    @Override
    public void close() {
        synchronized (this) {
            if (!sink.isClosed()) {
                sink.close();
            }
        }
    }

    /**
     * Checks whether this logger has been closed.
     *
     * @return {@code true} if the logger is closed, otherwise {@code false}
     */
    public boolean isClosed() {
        return sink.isClosed();
    }

    @Override
    public String toString() {
        return "Logger{" +
                "name='" + name + '\'' +
                ", sink=" + sink +
                ", minLevel=" + minLevel +
                '}';
    }
}
