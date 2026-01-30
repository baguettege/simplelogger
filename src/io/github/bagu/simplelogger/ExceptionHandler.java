package io.github.bagu.simplelogger;

/**
 * Handles exceptions that occur.
 * <p>
 * Implementations define how to respond to errors, such as logging, displaying error messages, or
 * recovering from failures.
 */
@FunctionalInterface
public interface ExceptionHandler {
    /**
     * Handles an exception.
     *
     * @param exception the exception to handle
     */
    void handle(Exception exception);
}
