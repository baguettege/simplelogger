package io.github.bagu.simplelogger.sinks;

import io.github.bagu.simplelogger.LogEvent;
import io.github.bagu.simplelogger.LogSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A log sink that forwards events to multiple underlying sinks.
 * <p>
 * This sink allows you to write the same log event to multiple destinations simultaneously,
 * such as both a file and the console. Events are forwarded to all sinks that are not closed.
 * <p>
 * Thread safety: This class is thread-safe. The {@link #accept(LogEvent)} method is not synchronized,
 * relying on the thread safety of the underlying sinks.
 *
 * @see LogSink
 */
public final class CompositeSink implements LogSink {
    private final List<LogSink> sinks;
    private volatile boolean closed = false;

    /**
     * Constructs a composite sink that forwards events to the specified sinks.
     *
     * @param sinks the sinks to forward events to
     * @throws NullPointerException if the array or any sink is null
     */
    public CompositeSink(LogSink... sinks) {
        List<LogSink> sinkList = new ArrayList<>();
        for (LogSink sink : sinks) {
            Objects.requireNonNull(sink);
            sinkList.add(sink);
        }
        this.sinks = sinkList;
    }

    /**
     * Accepts a log event and forwards it to all underlying sinks that are not closed.
     * <p>
     * If this composite sink is closed, the event is silently dropped.
     *
     * @param event the event to forward
     * @throws NullPointerException if event is null
     */
    @Override
    public void accept(LogEvent event) {
        Objects.requireNonNull(event);

        // all sinks must be thread-safe as this is not synchronized
        if (closed) return;

        for (LogSink sink : sinks) {
            if (!sink.isClosed()) {
                sink.accept(event);
            }
        }
    }

    /**
     * Closes this sink and all underlying sinks.
     * <p>
     * This method is idempotent and thread-safe. Already closed sinks are skipped.
     */
    @Override
    public void close() {
        synchronized (this) {
            if (closed) return;
            closed = true;

            for (LogSink sink : sinks) {
                if (!sink.isClosed()) {
                    sink.close();
                }
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public String toString() {
        return "CompositeSink{" +
                "sinks=" + sinks +
                ", closed=" + closed +
                '}';
    }
}
