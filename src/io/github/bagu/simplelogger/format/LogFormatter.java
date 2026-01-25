package io.github.bagu.simplelogger.format;

import io.github.bagu.simplelogger.LogEvent;

/**
 * A formatter that converts {@link LogEvent} instances into formatted string representations.
 *
 * <p>
 *     Implementations are responsible for converting raw event data into a human-readable string format, and
 *     must be thread-safe as they may be called concurrently.
 * </p>
 */
@FunctionalInterface
public interface LogFormatter {
    /**
     * Formats a log event into a string.
     *
     * @param event the log event to format
     * @return the formatted string representation of the event
     */
    String format(LogEvent event);
}
