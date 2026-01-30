package io.github.bagu.simplelogger.format.time;

import io.github.bagu.simplelogger.format.TimeFormatter;

import java.time.Instant;

/**
 * Formats timestamps using the ISO-8601 format.
 *
 * @see TimeFormatter
 */
public final class ISO8601TimeFormatter implements TimeFormatter {
    /**
     * Formats a timestamp in ISO-8601 format.
     *
     * @param timestamp the timestamp to format
     * @return the ISO-8601 formatted timestamp string
     */
    @Override
    public String format(Instant timestamp) {
        return timestamp.toString();
    }
}
