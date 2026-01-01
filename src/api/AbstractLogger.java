package api;

import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Abstract base class for asynchronous, thread-safe loggers.
 *
 * <p>
 *     {@link AbstractLogger} provides a framework for logging messages at multiple severity levels
 *     ({@code TRACE}, {@code DEBUG}, {@code INFO}, {@code WARN}, {@code ERROR}, {@code FATAL}) using a background logging thread and queue. Subclasses can
 *     configure the appearance of logs through a given {@link LogPresentation}, with an optional prefix.
 * </p>
 *
 * <p>
 *     Each {@link AbstractLogger} instance runs a singular daemon thread to continuously take log events from
 *     a {@link LinkedBlockingQueue} and print them to {@link System#out} or {@link System#err} depending on the severity.
 *     This is done to ensure that logging does not block the main thread for applications.
 * </p>
 *
 * <p>
 *     Once usage of the current instance has completed, {@link #close()} must be called (unless placed within a
 *     try-with-resources block) to drain all remaining logs and free up resources.
 * </p>
 *
 * <p>
 *     Subclasses may provide different {@link LogPresentation} instances and prefixes to customize ANSI color output,
 *     time formatting and message prefixes.
 * </p>
 */

public abstract class AbstractLogger implements AutoCloseable {
    private final Thread logThread;
    private final LinkedBlockingQueue<LogEvent> logQueue = new LinkedBlockingQueue<>();
    private volatile LogSeverity minimum = LogSeverity.INFO;

    private final LogPresentation logPresentation;
    private final String prefix;

    /**
     * Constructs a new {@link AbstractLogger} with a given {@link LogPresentation} and optional prefix.
     *
     * <p>
     *     This constructor begins a new daemon thread to continuously log events submitted to the logger's methods.
     * </p>
     *
     * <p>
     *     The provided {@link LogPresentation} controls the formatting of timestamps and whether ANSI color
     *     is outputted. The optional prefix will print in every log, unless set to {@code null}.
     * </p>
     *
     * <p>
     *     The default minimum log severity printed is {@link LogSeverity#INFO}.
     * </p>
     *
     * @param logPresentation the {@link LogPresentation} to define time formatting and ANSI output, must not be null
     * @param prefix the prefix to print to every log; can be {@code null} for no prefix
     */
    protected AbstractLogger(LogPresentation logPresentation, String prefix) {
        this.logPresentation = logPresentation;
        this.prefix = prefix;

        logThread = new Thread(this::logLoop, "Log-loop-" + System.identityHashCode(this));
        logThread.setDaemon(true);
        logThread.start();
    }

    /**
     * Sets the minimum severity for logs that can be logged. Any log below the specified severity will be ignored.
     *
     * <p>
     *     {@link LogSeverity#INFO} is set by default.
     * </p>
     *
     * @param severity minimum severity to log
     */
    public final void setMinimum(LogSeverity severity) {
        this.minimum = severity;
    }

    /**
     * Private helper method called by the public logging methods to create new {@link LogEvent}s based on the given
     * information and offer it to the queue.
     *
     * @param severity severity of the log
     * @param log message to log
     * @param throwable optional throwable to log; may be {@code null}
     */
    private void log(LogSeverity severity, String log, Throwable throwable) {
        LogEvent logEvent = new LogEvent(
                Instant.now(),
                severity,
                prefix,
                log,
                throwable,
                logPresentation
        );

        queueLogEvent(logEvent);
    }

    /**
     * Offers a given {@link LogEvent} to the queue.
     *
     * <p>
     *     {@link LogEvent}s with a lower {@link LogSeverity} than the set minimum will be ignored.
     * </p>
     *
     * @param logEvent event to log
     */
    private void queueLogEvent(LogEvent logEvent) {
        LogSeverity severity = logEvent.severity();
        if (severity.priority() < minimum.priority()) return;

        logQueue.offer(logEvent);
    }

    /**
     * Private helper method ran on a daemon thread to continuously log to the console.
     *
     * <p>
     *     This method takes a {@link LogEvent} from the queue, and directly logs it to the console.
     *     This happens indefinitely until the thread is interrupted.
     * </p>
     *
     * <p>
     *     Once the thread is interrupted, all remaining events in the queue will be drained and this method will exit.
     * </p>
     */
    private void logLoop() {
        try {
            while (true) {
                LogEvent logEvent = logQueue.take();
                logDirect(logEvent);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            LogEvent logEvent;
            while ((logEvent = logQueue.poll()) != null) {
                logDirect(logEvent);
            }
        }
    }

    /**
     * Private helper method to take a given {@link LogEvent}, format it and output to the console.
     *
     * <p>
     *     Events with a severity of {@link LogSeverity#ERROR} or above will be outputted to {@link System#err},
     *     otherwise {@link System#out}.
     * </p>
     *
     * @param logEvent event to log
     */
    private void logDirect(LogEvent logEvent) {
        String formatted = Util.formatEvent(logEvent);
        LogSeverity severity = logEvent.severity();

        if (severity == LogSeverity.ERROR || severity == LogSeverity.FATAL) {
            System.err.println(formatted);
            System.err.flush();
        } else {
            System.out.println(formatted);
        }
    }

    /**
     * Drains the {@link LogEvent} queue and ends the daemon thread.
     *
     * <p>
     *     Must be called to ensure resources are freed once this logger's usage is over.
     * </p>
     */
    @Override
    public final void close() {
        try {
            logThread.interrupt();
            logThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Logs a trace message to the console.
     *
     * @param log message to log
     */
    public final void trace(String log) {
        log(LogSeverity.TRACE, log, null);
    }

    /**
     * Logs a debug message to the console.
     *
     * @param log message to log
     */
    public final void debug(String log) {
        log(LogSeverity.DEBUG, log, null);
    }

    /**
     * Logs an informational message to the console.
     *
     * @param log message to log
     */
    public final void info(String log) {
        log(LogSeverity.INFO, log, null);
    }

    /**
     * Logs a warning message to the console.
     *
     * @param log message to log
     */
    public final void warn(String log) {
        log(LogSeverity.WARN, log, null);
    }

    /**
     * Logs an error message to the console.
     *
     * @param log message to log
     */
    public final void error(String log) {
        log(LogSeverity.ERROR, log, null);
    }

    /**
     * Logs a fatal message to the console.
     *
     * @param log message to log
     */
    public final void fatal(String log) {
        log(LogSeverity.FATAL, log, null);
    }

    /**
     * Logs a debug message to the console, along with a given {@link Throwable}'s stack trace.
     *
     * @param log message to log
     * @param throwable throwable stack trace to log
     */
    public final void debug(String log, Throwable throwable) {
        log(LogSeverity.DEBUG, log, throwable);
    }

    /**
     * Logs an error message to the console, along with a given {@link Throwable}'s stack trace.
     *
     * @param log message to log
     * @param throwable throwable stack trace to log
     */
    public final void error(String log, Throwable throwable) {
        log(LogSeverity.ERROR, log, throwable);
    }

    /**
     * Logs a fatal message to the console, along with a given {@link Throwable}'s stack trace.
     *
     * @param log message to log
     * @param throwable throwable stack trace to log
     */
    public final void fatal(String log, Throwable throwable) {
        log(LogSeverity.FATAL, log, throwable);
    }
}
