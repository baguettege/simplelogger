package io.github.bagu.simplelogger;

import io.github.bagu.simplelogger.sink.LogSink;

import java.util.List;
import java.util.Objects;

/**
 * A simple, thread-safe logger implementation that delegates log events to a {@link LogSink}.
 *
 * <p>
 *     This logger creates {@link LogEvent} instances with the current timestamp and thread name,
 *     then passes them to the given sink for processing.
 * </p>
 *
 * <p>
 *     This class is fully thread-safe.
 * </p>
 *
 * <pre>
 *     {@code SimpleLogger logger = new SimpleLogger("main", new PrintStreamLogSink(...));
 * logger.info("Hello world!");
 * logger.debug("Connected to server at {}", "192.168.0.1");
 * // application logic
 * logger.close();}
 * </pre>
 */
public final class SimpleLogger implements CloseableLogger {
    private final String name;
    private final LogSink sink;

    /**
     * Creates a new logger with the specified name and sink.
     *
     * @param name the logger name
     * @param sink the sink to send log events to
     */
    public SimpleLogger(String name, LogSink sink) {
        this.name = Objects.requireNonNull(name);
        this.sink = Objects.requireNonNull(sink);
    }

    /**
     * Private helper method which creates log events and delegates them to the sink.
     *
     * @param level the log level
     * @param message the message template
     * @param parameters parameters to substitute into the message template
     */
    private void log(
            Level level,
            String message,
            Object... parameters
    ) {
        LogEvent event = new LogEvent(
                System.currentTimeMillis(),
                level,
                Objects.requireNonNull(message),
                List.of(parameters),
                name,
                Thread.currentThread().getName()
        );

        sink.accept(event);
    }

    @Override
    public void trace(String message, Object... parameters) {
        this.log(Level.TRACE, message, parameters);
    }

    @Override
    public void debug(String message, Object... parameters) {
        this.log(Level.DEBUG, message, parameters);
    }

    @Override
    public void info(String message, Object... parameters) {
        this.log(Level.INFO, message, parameters);
    }

    @Override
    public void warn(String message, Object... parameters) {
        this.log(Level.WARN, message, parameters);
    }

    @Override
    public void error(String message, Object... parameters) {
        this.log(Level.ERROR, message, parameters);
    }

    @Override
    public void fatal(String message, Object... parameters) {
        this.log(Level.FATAL, message, parameters);
    }

    /**
     * Closes the logger by closing the underlying sink.
     *
     * <p>
     *     This method can be called multiple times safely. After closing, log events may be
     *     discarded depending on the sink implementation.
     * </p>
     */
    @Override
    public void close() {
        this.sink.close();
    }

    /**
     * Checks whether the underlying sink has been closed.
     *
     * @return {@code true} if the sink is closed, otherwise {@code false}
     */
    @Override
    public boolean isClosed() {
        return this.sink.isClosed();
    }
}
