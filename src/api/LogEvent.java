package api;

import java.time.Instant;

/**
 * Stores information for a specific log.
 *
 * @param instant timestamp of the log
 * @param severity severity of the log
 * @param prefix optional prefix for the log; may be {@code null}
 * @param log message to log
 * @param throwable optional throwable to log stack trace; may be {@code null}
 * @param logPresentation record storing information on how to present this log
 */

public record LogEvent(
        Instant instant,
        LogSeverity severity,
        String prefix,
        String log,
        Throwable throwable,
        LogPresentation logPresentation
) {}