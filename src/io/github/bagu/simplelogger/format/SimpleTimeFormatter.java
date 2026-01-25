package io.github.bagu.simplelogger.format;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * A time formatter that produces date and time in the format {@code HH:mm:ss}.
 *
 * <p>
 *     This formatter uses the system default timezone to convert epoch milliseconds into local time.
 * </p>
 */
public final class SimpleTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String format(long epochMillis) {
        return LocalTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                ZoneId.systemDefault()
                )
                .format(formatter);
    }
}
