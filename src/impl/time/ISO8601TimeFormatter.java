package impl.time;

import api.TimeFormatter;

import java.time.Instant;

/**
 * Implementation of {@link TimeFormatter}.
 *
 * <p>
 *     Formats a given {@link Instant} in an {@code ISO-8601} format.
 * </p>
 */

public final class ISO8601TimeFormatter implements TimeFormatter {

    /**
     * Returns a given {@link Instant} in {@code ISO-8601} format.
     *
     * @param instant {@link Instant} to format
     * @return instant in {@code ISO-8601} format
     */
    @Override
    public String format(Instant instant) {
        return instant.toString();
    }
}
