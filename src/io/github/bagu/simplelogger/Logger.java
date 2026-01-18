package io.github.bagu.simplelogger;

/**
 * A basic logging interface providing methods for logging at different severity levels.
 *
 * <p>
 *     This interface defines the core logging operations.
 *     Messages support parameter substitution.
 * </p>
 *
 *
 * <pre>
 *     {@code Logger logger = new SimpleLogger("main", sink);
 * logger.info("Hello world!");
 * logger.debug("Connected to server at {}", "192.168.0.1");}
 * </pre>
 */
public interface Logger {
    /**
     * Logs a message at the TRACE level.
     *
     * <p>
     *     Use TRACE for very detailed diagnostic information.
     * </p>
     *
     * @param message the message template
     * @param parameters parameters to substitute into the message template
     */
    void trace(String message, Object... parameters);

    /**
     * Logs a message at the DEBUG level.
     *
     * <p>
     *     Use DEBUG for information useful during development and debugging.
     * </p>
     *
     * @param message the message template
     * @param parameters parameters to substitute into the message template
     */
    void debug(String message, Object... parameters);

    /**
     * Logs a message at the INFO level.
     *
     * <p>
     *     Use INFO for informational messages about normal application operations.
     * </p>
     *
     * @param message the message template
     * @param parameters parameters to substitute into the message template
     */
    void info(String message, Object... parameters);

    /**
     * Logs a message at the WARN level.
     *
     * <p>
     *     Use WARN for potentially harmful situations that don't prevent the application
     *     from functioning but may require attention.
     * </p>
     *
     * @param message the message template
     * @param parameters parameters to substitute into the message template
     */
    void warn(String message, Object... parameters);

    /**
     * Logs a message at the ERROR level.
     *
     * <p>
     *     Use ERROR for error events that might still allow the application to continue running.
     * </p>
     *
     * @param message the message template
     * @param parameters parameters to substitute into the message template
     */
    void error(String message, Object... parameters);

    /**
     * Logs a message at the FATAL level.
     *
     * <p>
     *     Use FATAL for severe error events that will likely lead to application termination.
     * </p>
     *
     * @param message the message template
     * @param parameters parameters to substitute into the message template
     */
    void fatal(String message, Object... parameters);
}
