package io.github.bagu.simplelogger;

/**
 * Represents logging severity levels in ascending order of severity.
 * The levels are ordered from least severe (TRACE) to most severe (FATAL).
 *
 * <p>
 *     The ordinal values can be used for level comparison and filtering.
 * </p>
 */
public enum Level {
    /**
     * Detailed diagnostic information for troubleshooting.
     */
    TRACE,

    /**
     * Debugging information useful for development.
     */
    DEBUG,

    /**
     * Informational messages about application progress.
     */
    INFO,

    /**
     * Warning messages for potentially harmful situations.
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
