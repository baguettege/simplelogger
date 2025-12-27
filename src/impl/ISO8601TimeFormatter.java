package impl;

import api.TimeFormatter;

import java.time.Instant;

/**
 * Implementation of {@link TimeFormatter} which formats a given {@link Instant} into
 * an ISO-8601 representation.
 */

public final class ISO8601TimeFormatter implements TimeFormatter {

    /**
     * Returns the given {@link Instant} formatted in ISO-8601 format.
     *
     * @param instant the {@link Instant} to format
     * @return the {@link Instant} formatted in ISO-8601 format
     */
    @Override
    public String format(Instant instant) {
        return instant.toString();
    }
}
