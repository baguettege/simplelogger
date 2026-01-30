package io.github.bagu.simplelogger.sinks;

import io.github.bagu.simplelogger.LogEvent;
import io.github.bagu.simplelogger.LogSink;

/**
 * A log sink that discards all events.
 * <p>
 * This sink is useful for testing or temporarily disabling logging without changing logger
 * configurations. All methods are no-op.
 *
 * @see LogSink
 */
public final class NullSink implements LogSink {
    /**
     * Discards the log event without any action.
     *
     * @param event the event to discard
     */
    @Override
    public void accept(LogEvent event) {}

    /**
     * No-op close method.
     */
    @Override
    public void close() {}

    /**
     * Always returns {@code false} as this sink is never considered closed.
     *
     * @return {@code false}
     */
    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public String toString() {
        return "NullSink{}";
    }
}
