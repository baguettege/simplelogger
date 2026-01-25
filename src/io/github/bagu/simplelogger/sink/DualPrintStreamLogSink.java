package io.github.bagu.simplelogger.sink;

import io.github.bagu.simplelogger.Level;
import io.github.bagu.simplelogger.LogEvent;
import io.github.bagu.simplelogger.format.LogFormatter;

import java.io.PrintStream;
import java.util.Objects;

/**
 * A log sink that writes to two separate {@link PrintStream}s based on the log level.
 *
 * <p>
 *     This sink routes log events to different output streams:<br>
 *     - ERROR and FATAL levels -> {@code errStream}<br>
 *     - all other levels -> {@code outStream}
 * </p>
 *
 * <p>
 *     This is commonly used to send errors to {@link System#err} while sending normal logs to
 *     {@link System#out}, allowing proper handling of output redirection.
 * </p>
 *
 * <p>
 *     This class is fully thread-safe. All operations are synchronized.
 * </p>
 *
 * <pre>{@code
 * LogSink sink = new DualPrintStreamLogSink(
 *      System.out,
 *      System.err,
 *      new DefaultLogFormatter(...)
 * );
 *
 * // normal logs to stdout
 * logger.info("Hello world!");
 *
 * // errors to stderr
 * logger.error("Connection failed");
 * }</pre>
 */
public final class DualPrintStreamLogSink implements LogSink {
    private final Object lock = new Object();
    private volatile boolean isClosed = false;
    private final PrintStream outStream;
    private final PrintStream errStream;
    private final LogFormatter formatter;

    /**
     * Creates a new dual print stream sink.
     *
     * @param outStream the stream for non-error log events
     * @param errStream the stream for ERROR and FATAL events
     * @param formatter the formatter for converting events to strings
     */
    public DualPrintStreamLogSink(
            PrintStream outStream,
            PrintStream errStream,
            LogFormatter formatter
    ) {
        this.outStream = Objects.requireNonNull(outStream);
        this.errStream = Objects.requireNonNull(errStream);
        this.formatter = Objects.requireNonNull(formatter);
    }

    /**
     * Checks if the output stream has encountered an error.
     *
     * @return {@code true} if the output stream has an error, otherwise {@code false}
     */
    public boolean checkOutError() {
        return outStream.checkError();
    }

    /**
     * Checks if the error stream has encountered an error.
     *
     * @return {@code true} if the error stream has an error, otherwise {@code false}
     */
    public boolean checkErrError() {
        return errStream.checkError();
    }

    @Override
    public void accept(LogEvent event) {
        String formatted = formatter.format(event);

        synchronized (lock) {
            if (isClosed) return;

            if (event.level() == Level.ERROR || event.level() == Level.FATAL)
                errStream.println(formatted);
            else
                outStream.println(formatted);
        }
    }

    /**
     * Closes both streams unless they are {@link System#out} or {@link System#err}.
     *
     * <p>
     *     This method protects against closing standard streams to prevent breaking other
     *     parts of the application.
     * </p>
     *
     * <p>
     *     This method can be called multiple times safely.
     * </p>
     */
    @Override
    public void close() {
        synchronized (lock) {
            if (isClosed) return;
            isClosed = true;

            if (outStream != System.out && outStream != System.err)
                outStream.close();
            if (errStream != System.out && errStream != System.err)
                errStream.close();
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }
}
