package api;

import impl.ISO8601TimeFormatter;

import java.io.PrintStream;
import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Configurable logging utility that supports multiple severity levels, ANSI color output, prefixing and customizable
 * time formatting with the {@link TimeFormatter} interface.
 * <p>
 *     It supports the following severity levels:
 *     {@code TRACE}, {@code DEBUG}, {@code INFO}, {@code WARN}, {@code ERROR}, and {@code FATAL}.
 * </p>
 * <p>
 *     Messages can include {@link Throwable}s to log their stack traces, and output streams can be customized.
 * </p>
 * <p>
 *     Default settings are:
 *     <li>No prefixes</li>
 *     <li>ANSI disabled</li>
 *     <li>Severity level at {@code INFO}</li>
 *     <li>Time formatter formats in ISO-8601 format</li>
 *     <li>{@link PrintStream}s are {@code System.out} and {@code System.err}</li>
 * </p>
 */

public class SimpleLogger implements AutoCloseable {
    private volatile String[] prefixes = new String[0];
    private volatile boolean ansiEnabled = false;
    private volatile SeverityLevel level = SeverityLevel.INFO;
    private volatile TimeFormatter timeFormatter = new ISO8601TimeFormatter();
    private volatile PrintStream out = System.out;
    private volatile PrintStream err = System.err;

    private record LogEvent(
            Instant timestamp,
            SeverityLevel severity,
            String message,
            Throwable throwable,
            String[] additionalPrefixes
    ) {}
    private static final int DEFAULT_QUEUE_SIZE = 10_000;
    private final LinkedBlockingQueue<LogEvent> logQueue;
    private final Thread logThread;
    private volatile boolean running = true;

    private final AtomicLong droppedCount = new AtomicLong(0);
    private volatile long lastDroppedReportTime = System.nanoTime(); // not thread-safe, but is only ever called from a single thread so is fine
    private static final long DROP_REPORT_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(10);

    /**
     * Constructs a new {@link SimpleLogger} instance with a specified queue size.
     * <p>
     *     This will summon a new worker thread for the new instance created.
     * </p>
     * <p>
     *     A queue size of {@code -1} will use the default queue size.
     * </p>
     */
    public SimpleLogger(int queueSize) {
        queueSize = (queueSize != -1) ? queueSize : DEFAULT_QUEUE_SIZE;
        logQueue = new LinkedBlockingQueue<>(queueSize);
        logThread = new Thread(this::logLoop, "Log-loop-" + System.identityHashCode(this));
        logThread.setDaemon(true);
        logThread.start();
    }

    /**
     * Constructs a new {@link SimpleLogger} instance with the default queue size.
     * <p>
     *     This will summon a new worker thread for the new instance created.
     * </p>
     */
    public SimpleLogger() {
        this(-1);
    }

    /**
     * Returns a new instance of a {@link SimpleLogger} with the copied instance's configuration.
     * <p>
     *     This will summon a new worker thread for the new instance created.
     * </p>
     *
     * @return new instance with same configuration
     */
    public SimpleLogger copy() {
        return new SimpleLogger()
                .setPrefixes(this.prefixes)
                .setAnsiEnabled(this.ansiEnabled)
                .setLevel(this.level)
                .setTimeFormatter(this.timeFormatter)
                .setOut(this.out)
                .setErr(this.err);
    }

    /**
     * Sets global prefixes that are prepended to every log message.
     * <p>
     *     No prefixes are set by default.
     * </p>
     *
     * @param prefixes one or more prefix strings
     * @return the current {@link SimpleLogger} instance
     */
    public SimpleLogger setPrefixes(String... prefixes) {
        this.prefixes = prefixes.clone();
        return this;
    }

    /**
     * Appends a global prefix to every log message.
     *
     * @param prefixes one or more prefix strings
     * @return the current {@link SimpleLogger} instance
     */
    public SimpleLogger addPrefixes(String... prefixes) {
        if (prefixes == null || prefixes.length == 0) return this;

        String[] newPrefixes = new String[this.prefixes.length + prefixes.length];
        System.arraycopy(this.prefixes, 0, newPrefixes, 0, this.prefixes.length);
        System.arraycopy(prefixes, 0, newPrefixes, this.prefixes.length, prefixes.length);

        setPrefixes(newPrefixes);
        return this;
    }

    /**
     * Returns the current prefixes used by this {@link SimpleLogger} instance.
     *
     * @return prefixes used by this instance
     */
    public String[] getPrefixes() {
        return this.prefixes.clone();
    }

    /**
     * Removes all current set prefixes for the {@link SimpleLogger} instance.
     *
     * @return the current {@link SimpleLogger} instance
     */
    public SimpleLogger removePrefixes() {
        this.prefixes = new String[0];
        return this;
    }

    /**
     * Enables or disables ANSI color codes in log outputs.
     * <p>
     *     Disabled by default.
     * </p>
     *
     * @param enabled {@code true} to enable, {@code false} to disable.
     * @return the current {@link SimpleLogger} instance
     */
    public SimpleLogger setAnsiEnabled(boolean enabled) {
        this.ansiEnabled = enabled;
        return this;
    }

    /**
     * Sets the minimum severity level of messages to log. Logs with lower severity will be ignored.
     * <p>
     *     Set to {@code INFO} by default.
     * </p>
     *
     * @param level minimum severity level of messages to log
     * @return the current {@link SimpleLogger} instance
     */
    public SimpleLogger setLevel(SeverityLevel level) {
        this.level = level;
        return this;
    }

    /**
     * Sets the formatter used for timestamping logs.
     *
     * @param formatter an implementation of {@link TimeFormatter}
     * @return the current {@link SimpleLogger} instance
     */
    public SimpleLogger setTimeFormatter(TimeFormatter formatter) {
        this.timeFormatter = formatter;
        return this;
    }

    /**
     * Sets the output stream for non-error messages.
     * <p>
     *     Set to {@code System.out} by default.
     * </p>
     *
     * @param out the {@link PrintStream} to write regular messages
     * @return the current {@link SimpleLogger} instance
     */
    public SimpleLogger setOut(PrintStream out) {
        this.out = out;
        return this;
    }

    /**
     * Sets the output stream for error messages.
     * <p>
     *     Set to {@code System.err} by default.
     * </p>
     *
     * @param err the {@link PrintStream} to write error messages
     * @return the current {@link SimpleLogger} instance
     */
    public SimpleLogger setErr(PrintStream err) {
        this.err = err;
        return this;
    }

    // logging

    /**
     * Called by public logging methods. Creates an internal {@link LogEvent} instance for this log,
     * and attempts to queue it. If the queue is full due to backpressure, the log will be dropped unless
     * in cases where the severity is {@code FATAL} or {@code ERROR}, where it will instead be logged
     * synchronously.
     *
     * @param message message to log
     * @param throwable optional throwable to log, can be null
     * @param severity severity of the log
     * @param additionalPrefixes optional additional prefixes for the log
     */
    private void queueLog(
            String message,
            Throwable throwable,
            SeverityLevel severity,
            String[] additionalPrefixes
    ) {
        if (!running) return;
        if (severity.priority() < level.priority()) return;

        additionalPrefixes = additionalPrefixes == null ? new String[0] : additionalPrefixes.clone();

        LogEvent event = new LogEvent(
                Instant.now(),
                severity,
                message,
                throwable,
                additionalPrefixes
        );

        // drop logs if the queue is full
        if (!logQueue.offer(event)) {
            if (severity.priority() >= SeverityLevel.ERROR.priority()) {
                log(event); // log synchronously if a serious log
            } else {
                droppedCount.incrementAndGet();
            }
        }
    }

    /**
     * Method ran on a designated thread, using the MPSC pattern to read queued logs, formats them
     * and outputs them to the {@link PrintStream}.
     */
    private void logLoop() {
        try {
            while (running || !logQueue.isEmpty()) {
                maybeLogDroppedReport();

                LogEvent event = logQueue.poll(100, TimeUnit.MILLISECONDS);
                if (event != null) log(event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            LogEvent event;
            while ((event = logQueue.poll()) != null) {
                log(event);
            }
        }
    }

    /**
     * Takes a {@link LogEvent} instance and formats it into a readable string of text, then outputs
     * it to the {@link PrintStream}.
     *
     * @param event log event to log
     */
    private void log(LogEvent event) {
        SeverityLevel severity = event.severity;

        StringBuilder finalOutput = new StringBuilder(event.message);

        String color = ansiEnabled ? Util.colorFor(severity) : "";
        String time = timeFormatter.format(event.timestamp);
        String level = severity.name();

        String indent = Util.buildIndent(
                color,
                this.ansiEnabled,
                time,
                level,
                this.prefixes,
                event.additionalPrefixes
        );

        if (event.throwable != null)
            finalOutput.append("\n")
                    .append(color)
                    .append(Util.captureStackTrace(event.throwable))
                    .append(ansiEnabled ? AnsiConstants.RESET : "");

        String fmt = Util.format(finalOutput.toString(), indent);

        if (severity == SeverityLevel.ERROR || severity == SeverityLevel.FATAL) {
            err.println(fmt);
            err.flush();
        } else {
            out.println(fmt);
        }
    }

    /**
     * Method to synchronously log to the {@link PrintStream} the number of dropped logs,
     * in specified intervals. If no logs have been dropped, nothing is outputted.
     */
    private void maybeLogDroppedReport() {
        long now = System.nanoTime();
        long last = lastDroppedReportTime;

        long elapsed = now - last;
        if (elapsed < DROP_REPORT_INTERVAL_NANOS) return;

        long dropped = droppedCount.getAndSet(0);
        if (dropped == 0) return;

        lastDroppedReportTime = now;

        LogEvent event = new LogEvent(
                Instant.now(),
                SeverityLevel.WARN,
                "Logger dropped " + dropped + " logs due to backpressure",
                null,
                new String[0]
        );

        log(event); // synchronous so never dropped
    }

    /**
     * Drains all current logs and stops the internal worker thread for this {@link SimpleLogger}.
     */
    @Override
    public void close() {
        running = false;
        logThread.interrupt();
        try {
            logThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (logThread.isAlive()) {
            log(new LogEvent(
                    Instant.now(),
                    SeverityLevel.ERROR,
                    "Log draining timeout occurred; logs may be lost",
                    null,
                    new String[0]
            ));
        }
    }

    // method overloading

    /**
     * Logs an {@code INFO} level message.
     *
     * @param message message to log
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void info(String message, String... prefixes) {
        queueLog(
                message,
                null,
                SeverityLevel.INFO,
                prefixes
        );
    }

    /**
     * Logs a {@code WARN} level message.
     *
     * @param message message to log
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void warn(String message, String... prefixes) {
        queueLog(
                message,
                null,
                SeverityLevel.WARN,
                prefixes
        );
    }

    /**
     * Logs a {@code WARN} level message with a {@link Throwable}.
     *
     * @param message message to log
     * @param throwable the {@link Throwable} to log.
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void warn(String message, Throwable throwable, String... prefixes) {
        queueLog(
                message,
                throwable,
                SeverityLevel.WARN,
                prefixes
        );
    }

    /**
     * Logs an {@code ERROR} level message.
     *
     * @param message message to log
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void error(String message, String... prefixes) {
        queueLog(
                message,
                null,
                SeverityLevel.ERROR,
                prefixes
        );
    }

    /**
     * Logs an {@code ERROR} level message with a {@link Throwable}.
     *
     * @param message message to log
     * @param throwable the {@link Throwable} to log.
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void error(String message, Throwable throwable, String... prefixes) {
        queueLog(
                message,
                throwable,
                SeverityLevel.ERROR,
                prefixes
        );
    }

    /**
     * Logs a {@code FATAL} level message.
     *
     * @param message message to log
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void fatal(String message, String... prefixes) {
        queueLog(
                message,
                null,
                SeverityLevel.FATAL,
                prefixes
        );
    }

    /**
     * Logs a {@code FATAL} level message with a {@link Throwable}.
     *
     * @param message message to log
     * @param throwable the {@link Throwable} to log.
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void fatal(String message, Throwable throwable, String... prefixes) {
        queueLog(
                message,
                throwable,
                SeverityLevel.FATAL,
                prefixes
        );
    }

    /**
     * Logs a {@code TRACE} level message.
     *
     * @param message message to log
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void trace(String message, String... prefixes) {
        queueLog(
                message,
                null,
                SeverityLevel.TRACE,
                prefixes
        );
    }

    /**
     * Logs a {@code DEBUG} level message.
     *
     * @param message message to log
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void debug(String message, String... prefixes) {
        queueLog(
                message,
                null,
                SeverityLevel.DEBUG,
                prefixes
        );
    }

    /**
     * Logs a {@code DEBUG} level message with a {@link Throwable}.
     *
     * @param message message to log
     * @param throwable the {@link Throwable} to log.
     * @param prefixes optional, additional prefixes to prepend to the log
     */
    public void debug(String message, Throwable throwable, String... prefixes) {
        queueLog(
                message,
                throwable,
                SeverityLevel.DEBUG,
                prefixes
        );
    }
}
