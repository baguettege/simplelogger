package io.github.bagu.simplelogger.sink;

import io.github.bagu.simplelogger.LogEvent;
import io.github.bagu.simplelogger.format.LogFormatter;

import java.io.PrintStream;
import java.util.Objects;

/**
 * A simple log sink that writes formatted log events to a {@link PrintStream}.
 *
 * <p>
 *     This is the most basic sink implementation, writing each log event to the specified stream. It is
 *     commonly used with {@link System#out} or {@link System#err}, or with file-based {@link PrintStream}s.
 * </p>
 *
 * <p>
 *     This class is fully thread-safe. All operations are synchronized.
 * </p>
 *
 * <pre>{@code
 * // console logging
 * LogSink sink = new PrintStreamLogSink(
 *      System.out,
 *      new DefaultLogFormatter(...)
 * );
 * }</pre>
 */
public final class PrintStreamLogSink implements LogSink {
    private final Object lock = new Object();
    private volatile boolean isClosed = false;
    private final PrintStream stream;
    private final LogFormatter formatter;

    /**
     * Creates a new print stream sink.
     *
     * @param stream the stream to write formatted events to
     * @param formatter the formatter for converting events to strings
     */
    public PrintStreamLogSink(PrintStream stream, LogFormatter formatter) {
        this.stream = Objects.requireNonNull(stream);
        this.formatter = Objects.requireNonNull(formatter);
    }

    /**
     * Checks if the stream has encountered an error.
     *
     * <p>
     *     This delegates to {@link PrintStream#checkError()}.
     * </p>
     *
     * @return {@code true} if the stream has an error, otherwise {@code false}
     */
    public boolean checkError() {
        return stream.checkError();
    }

    @Override
    public void accept(LogEvent event) {
        String formatted = formatter.format(event);

        synchronized (lock) {
            if (isClosed) return;
            stream.println(formatted);
        }
    }

    /**
     * Closes the stream unless it is {@link System#out} or {@link System#err}.
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

            if (stream != System.out && stream != System.err)
                stream.close();
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }
}
