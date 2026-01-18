package io.github.bagu.simplelogger.format;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * A time formatter that produces date and time in the format {@code yyyy-MM-dd HH:mm:ss}.
 *
 * <p>
 *     This formatter uses the system default timezone to convert epoch milliseconds into local date-time.
 * </p>
 */
public final class SimpleDateTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(long epochMillis) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                ZoneId.systemDefault()
                )
                .format(formatter);
    }
}
