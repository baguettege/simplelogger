package io.github.bagu.simplelogger.format;

/**
 * A formatter for converting epoch milliseconds into human-readable timestamp strings.
 *
 * <p>
 *     Implementations of this interface are responsible for converting Unix timestamps into formatted
 *     time strings.
 * </p>
 */
@FunctionalInterface
public interface TimeFormatter {
    /**
     * Formats a timestamp into a string.
     *
     * @param epochMillis the timestamp in milliseconds since the Unix epoch
     * @return the formatted timestamp string
     */
    String format(long epochMillis);
}
