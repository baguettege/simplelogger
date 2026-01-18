package io.github.bagu.simplelogger;

/**
 * A logger that can be closed and implements {@link AutoCloseable} for use in try-with-resources.
 *
 * <p>
 *     This interface extends both {@link Logger} and {@link AutoCloseable}, allowing loggers to properly
 *     release resources when closed.
 * </p>
 *
 * <p>
 *     Implementations must ensure thread-safe close operations.
 * </p>
 *
 * <pre>
 *     {@code try (CloseableLogger logger = new SimpleLogger("main", sink)) {
 *     logger.info("Hello world!");
 *     // application logic
 * } // logger automatically closed}
 * </pre>
 */
public interface CloseableLogger extends Logger, AutoCloseable {
    /**
     * Closes this logger and releases any resources.
     *
     * <p>
     *     After calling this method, the logger should not be used for further logging operations.
     *     Implementations must ensure that this method can be called multiple times safely.
     * </p>
     */
    @Override void close();

    /**
     * Checks whether this logger has been closed.
     *
     * @return {@code true} if this logger is closed, otherwise {@code false}
     */
    boolean isClosed();
}
