package impl;

import api.TimeFormatter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link TimeFormatter} which formats a given {@link Instant} into
 * an {@code HH:mm:ss} representation.
 */

public class LocalTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Returns the given {@link Instant} formatted in a {@code HH:mm:ss} format.
     *
     * @param instant the {@link Instant} to format
     * @return the {@link Instant} formatted in a {@code HH:mm:ss} format
     */
    @Override
    public String format(Instant instant) {
        return LocalTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }
}
