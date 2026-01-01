package impl.time;

import api.TimeFormatter;

import java.time.Instant;

public final class ISO8601TimeFormatter implements TimeFormatter {
    @Override
    public String format(Instant instant) {
        return instant.toString();
    }
}
