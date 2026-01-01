package api;

import java.time.Instant;

public record LogEvent(
        Instant instant,
        LogSeverity severity,
        String prefix,
        String log,
        Throwable throwable,
        LogPresentation logPresentation
) {}