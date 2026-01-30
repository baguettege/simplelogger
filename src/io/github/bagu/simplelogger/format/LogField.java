package io.github.bagu.simplelogger.format;

/**
 * Represents the fields that can be included in formatted log output.
 * <p>
 * These fields define what metadata appears in the log prefix before the message. Fields are
 * typically displayed in square brackets in the order specified.
 */
public enum LogField {
    /**
     * The timestamp when the log event occurred.
     */
    TIMESTAMP,

    /**
     * The severity level of the log event.
     */
    LEVEL,

    /**
     * The name of the thread that generated the log event.
     */
    THREAD_NAME,

    /**
     * The name of the logger that generated the log event.
     */
    LOGGER_NAME
}
