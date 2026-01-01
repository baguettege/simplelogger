package api;

import java.time.Instant;

@FunctionalInterface
public interface TimeFormatter {
    String format(Instant instant);
}
