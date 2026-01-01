package impl.time;

import api.TimeFormatter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link TimeFormatter}.
 *
 * <p>
 *     Formats a given {@link Instant} in an {@code HH:mm:ss} format.
 * </p>
 */

public final class SimpleTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Returns a given {@link Instant} in {@code HH:mm:ss} format.
     *
     * @param instant {@link Instant} to format
     * @return instant in {@code HH:mm:ss} format
     */
    @Override
    public String format(Instant instant) {
        LocalTime time = LocalTime.ofInstant(instant, ZoneId.systemDefault());
        return time.format(timeFormatter);
    }
}
