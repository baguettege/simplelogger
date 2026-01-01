package api;

import java.time.Instant;

/**
 * Functional interface for the formatting of {@link Instant} timestamps.
 *
 * <p>
 *     Takes in an {@link Instant} and returns a {@link String} representation of that instant. The representation
 *     is defined by the implementation.
 * </p>
 */

@FunctionalInterface
public interface TimeFormatter {

    /**
     * Returns a {@link String} representation of a specified {@link Instant}.
     *
     * @param instant {@link Instant} to format
     * @return {@link String} representation of the instance
     */
    String format(Instant instant);
}
