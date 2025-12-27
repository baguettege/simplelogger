package impl;

import api.TimeFormatter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link TimeFormatter} which formats a given {@link Instant} into
 * an {@code yyyy-MM-dd HH-mm-ss} representation.
 */

public class LocalDateTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    /**
     * Returns the given {@link Instant} formatted in a {@code yyyy-MM-dd HH-mm-ss} format.
     *
     * @param instant the {@link Instant} to format
     * @return the {@link Instant} formatted in a {@code yyyy-MM-dd HH-mm-ss} format
     */
    @Override
    public String format(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }
}
