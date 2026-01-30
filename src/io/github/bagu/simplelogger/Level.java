package io.github.bagu.simplelogger;

/**
 * Represents the severity level of a log message.
 * <p>
 * The severity of multiple levels can be compared using {@link Enum#ordinal()}, where a larger
 * number indicates a higher severity.
 *
 * @see LogEvent#level()
 * @see Logger
 * @see io.github.bagu.simplelogger.sinks.FilteredSink
 */
public enum Level {
    /**
     * Fine-grained, highly detailed logs.
     */
    TRACE,

    /**
     * Fine-grained, informational logs used for debugging an application.
     */
    DEBUG,

    /**
     * Informational messages about application progress.
     */
    INFO,

    /**
     * Warning messages for potentially harmful, but recoverable situations.
     */
    WARN,

    /**
     * Error events that might still allow the application to continue.
     */
    ERROR,

    /**
     * Severe error events that will likely lead to the application terminating.
     */
    FATAL
}
