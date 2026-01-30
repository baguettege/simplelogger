package io.github.bagu.simplelogger.format.time;

import io.github.bagu.simplelogger.format.TimeFormatter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Formats timestamps as local date and time.
 * <p>
 * This formatter produces timestamps in the format {@code yyyy-MM-dd HH:mm:ss} using
 * the system's default timezone.
 *
 * @see TimeFormatter
 */
public final class LocalDateTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Formats a timestamp as local date and time in the system's default time zone.
     *
     * @param timestamp the timestamp to format
     * @return the formatted date and time string
     */
    @Override
    public String format(Instant timestamp) {
        return LocalDateTime.ofInstant(
                timestamp,
                ZoneId.systemDefault()
        ).format(formatter);
    }
}
