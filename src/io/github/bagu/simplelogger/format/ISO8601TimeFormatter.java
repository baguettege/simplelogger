package io.github.bagu.simplelogger.format;

import java.time.Instant;

/**
 * A time formatter that produces ISO 8601 formatted timestamps.
 */
public final class ISO8601TimeFormatter implements TimeFormatter {
    @Override
    public String format(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).toString();
    }
}
