package io.github.bagu.simplelogger.format;

import java.time.Instant;

/**
 * Formats timestamps for log output.
 * <p>
 * Implementations define how timestamps are rendered in log events, such as ISO-8601
 * format, local time, or custom formats.
 *
 * @see LogFormatter
 * @see io.github.bagu.simplelogger.format.time.ISO8601TimeFormatter
 * @see io.github.bagu.simplelogger.format.time.LocalTimeFormatter
 * @see io.github.bagu.simplelogger.format.time.LocalDateTimeFormatter
 */
@FunctionalInterface
public interface TimeFormatter {
    /**
     * Formats a timestamp into a string.
     *
     * @param timestamp the timestamp to format
     * @return the formatted timestamp string
     */
    String format(Instant timestamp);
}
