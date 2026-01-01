package impl.time;

import api.TimeFormatter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link TimeFormatter}.
 *
 * <p>
 *     Formats a given {@link Instant} in an {@code yyyy-MM-dd HH-mm-ss} format.
 * </p>
 */

public final class SimpleDateTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    /**
     * Returns a given {@link Instant} in {@code yyyy-MM-dd HH-mm-ss} format.
     *
     * @param instant {@link Instant} to format
     * @return instant in {@code yyyy-MM-dd HH-mm-ss} format
     */
    @Override
    public String format(Instant instant) {
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return time.format(timeFormatter);
    }
}
