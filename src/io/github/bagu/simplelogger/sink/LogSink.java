package io.github.bagu.simplelogger.sink;

import io.github.bagu.simplelogger.LogEvent;

/**
 * A sink that accepts and processes {@link LogEvent}s.
 *
 * <p>
 *     Log sinks are responsible for the final destination and formatting log events. Common
 *     implementations include writing to console, files, network sockets, or databases. Sinks
 *     can also be composed, filtered, or made asynchronous.
 * </p>
 *
 * <p>
 *     All sink implementations must be thread-safe as they may receive events from multiple threads consecutively.
 * </p>
 *
 * <pre>{@code
 * // simple console sink
 * LogSink sink = new PrintStreamLogSink(System.out, formatter);
 *
 * // filtered sink (only errors)
 * LogSink errorSink = FilterLogSink.filterByMinLevel(sink, Level.ERROR);
 *
 * // asynchronous sink
 * LogSink asyncSink = AsyncLogSink.builder()
 *     .sink(sink)
 *     .queueSize(2048)
 *     .build();
 * }</pre>
 */
public interface LogSink extends AutoCloseable {
    /**
     * Accepts and processes a log event.
     *
     * <p>
     *     Implementations should handle this event appropriately (format and write it, queue it, filter it, etc.).
     *     This method should not throw exceptions under normal circumstances.
     * </p>
     *
     * <p>
     *     This method must be thread-safe.
     * </p>
     *
     * @param event the log event to process
     */
    void accept(LogEvent event);


    /**
     * Closes this sink and releases any resources.
     *
     * <p>
     *     After calling this method, the sink should not accept new events. Implementations must ensure this method can
     *     be called multiple times safely.
     * </p>
     */
    @Override void close();

    /**
     * Checks whether the sink has been closed.
     *
     * @return {@code true} if this sink is closed, otherwise {@code false}
     */
    boolean isClosed();
}
