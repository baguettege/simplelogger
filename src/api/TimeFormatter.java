package api;

import java.time.Instant;

/**
 * Functional interface used for formatting {@link Instant} values as strings.
 * <p>
 *     Used for the formatting of timestamps within {@link SimpleLogger} instances.
 * </p>
 * <p>
 *     Concrete implementations should define how the passed {@link Instant} should be represented
 *     as a string of text.
 * </p>
 */

@FunctionalInterface
public interface TimeFormatter {

    /**
     * Formats a given {@link Instant} into a string of text.
     *
     * @param instant the {@link Instant} to format
     * @return formatted string representing the given {@link Instant}
     */
    String format(Instant instant);
}
