package io.github.bagu.simplelogger.sinks;

import io.github.bagu.simplelogger.LogEvent;
import io.github.bagu.simplelogger.LogSink;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A log sink that filters events before forwarding them to an underlying sink.
 * <p>
 * Only events that satisfy the predicate are forwarded. This allows you to implement custom
 * filtering logic, such as filtering by level, logger name, thread, or any combination of event
 * properties.
 * <p>
 * Thread safety: This class is thread-safe so long as the underlying sink and predicate are
 * thread safe themselves.
 * <p>
 * Notes:
 * <ol>
 * <li>The predicate must not throw any unchecked exceptions.</li>
 * </ol>
 *
 * @see LogSink
 * @see LogEvent
 */
public final class FilteredSink implements LogSink {
    private final LogSink sink;
    private final Predicate<LogEvent> predicate;

    /**
     * Constructs a filtered sink with the specified underlying sink and filter predicate.
     * <p>
     * The predicate must not throw any unchecked exceptions.
     *
     * @param sink the underlying sink to forward events to
     * @param predicate the filter predicate, events are forwarded if this returns {@code true}
     * @throws NullPointerException if any parameter is null
     */
    public FilteredSink(LogSink sink, Predicate<LogEvent> predicate) {
        this.sink = Objects.requireNonNull(sink);
        this.predicate = Objects.requireNonNull(predicate);
    }

    /**
     * Accepts a log event and forwards it to the underlying sink if it matches the predicate.
     *
     * @param event the event to write
     * @throws NullPointerException if the event is null
     */
    @Override
    public void accept(LogEvent event) {
        Objects.requireNonNull(event);

        if (predicate.test(event)) {
            sink.accept(event);
        }
    }

    @Override
    public void close() {
        sink.close();
    }

    @Override
    public boolean isClosed() {
        return sink.isClosed();
    }

    @Override
    public String toString() {
        return "FilteredSink{" +
                "sink=" + sink +
                ", predicate=" + predicate +
                '}';
    }
}
