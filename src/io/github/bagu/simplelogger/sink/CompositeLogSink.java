package io.github.bagu.simplelogger.sink;

import io.github.bagu.simplelogger.LogEvent;

import java.util.List;

/**
 * A composite sink that forwards log events to multiple child sinks.
 *
 * <p>
 *     This sink allows you to send the same log event to multiple destinations simultaneously, such as
 *     both console and file. Each child sink that is not closed will receive every log event.
 * </p>
 *
 * <p>
 *     This class is fully thread safe. All operations are synchronized.
 * </p>
 *
 * <pre>{@code
 * LogSink compositeSink = new CompositeLogSink(
 *      new PrintStreamLogSink(...),
 *      new DualPrintStreamLogSink(...)
 * );
 *
 * // log events are sent to both sinks
 * compositeSink.accept(event);
 *
 * // closing composite closes all children
 * compositeSink.close();
 * }</pre>
 */
public final class CompositeLogSink implements LogSink {
    private final Object lock = new Object();
    private volatile boolean isClosed = false;
    private final List<LogSink> sinks;

    /**
     * Creates a composite sink that forwards events to all specified child sinks.
     *
     * @param sinks the child sinks to forward events to
     */
    public CompositeLogSink(LogSink... sinks) {
        this.sinks = List.of(sinks);
    }

    /**
     * Forwards the log event to all non-closed child sinks.
     *
     * @param event the log event to forward
     */
    @Override
    public void accept(LogEvent event) {
        synchronized (lock) {
            if (isClosed) return;

            for (LogSink sink : sinks)
                if (!sink.isClosed())
                    sink.accept(event);
        }
    }

    /**
     * Closes this sink and all non-closed child sinks.
     */
    @Override
    public void close() {
        synchronized (lock) {
            if (isClosed) return;
            isClosed = true;

            for (LogSink sink : sinks)
                if (!sink.isClosed())
                    sink.close();
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }
}
