package io.github.bagu.simplelogger.format.time;

import io.github.bagu.simplelogger.format.TimeFormatter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Formats timestamps as local time.
 * <p>
 * This formatter produces timestamps in the format {@code HH:mm:ss} using
 * the system's default timezone.
 *
 * @see TimeFormatter
 */
public final class LocalTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Formats a timestamp as local time in the system's default time zone.
     *
     * @param timestamp the timestamp to format
     * @return the formatted time string
     */
    @Override
    public String format(Instant timestamp) {
        return LocalTime.ofInstant(
                timestamp,
                ZoneId.systemDefault()
        ).format(formatter);
    }
}
