package io.github.bagu.simplelogger.sinks;

import io.github.bagu.simplelogger.ExceptionHandler;
import io.github.bagu.simplelogger.LogEvent;
import io.github.bagu.simplelogger.format.LogFormatter;
import io.github.bagu.simplelogger.LogSink;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * A log sink that writes formatted log events to a file.
 * <p>
 * This sink buffers writes and flushes to disk periodically to balance performance.
 * <p>
 * Thread safety: This class is thread-safe. Writes are synchronized to prevent interleaving of
 * log entries.
 *
 * @see LogSink
 * @see LogFormatter
 * @see ExceptionHandler
 */
public final class FileSink implements LogSink {
    private final LogFormatter formatter;
    private final ExceptionHandler exceptionHandler;
    private final int flushEvery;

    private final BufferedWriter writer;
    private volatile boolean closed = false;
    private volatile int count = 0;

    /**
     * Constructs a file sink that appends to the specified path.
     *
     * @param path the file path to write to, is created if it doesn't exist
     * @param exceptionHandler handler for I/O exceptions that occur during writing
     * @param flushEvery how many events to buffer before flushing to disk
     * @param formatter the formatter to use for rendering log events
     * @throws IOException if the file cannot be opened for writing
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if flushEvery is less than 1
     */
    public FileSink(
            Path path,
            ExceptionHandler exceptionHandler,
            int flushEvery,
            LogFormatter formatter
    ) throws IOException {
        Objects.requireNonNull(path);
        if (flushEvery < 1) {
            throw new IllegalArgumentException("Flush every < 1: " + flushEvery);
        }

        this.formatter = Objects.requireNonNull(formatter);
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler);
        this.flushEvery = flushEvery;

        this.writer = Files.newBufferedWriter(
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    /**
     * Accepts a log event, formats it, and writes it to the file.
     * <p>
     * The event is formatted outside the synchronized block for better concurrency.
     * If the number of buffered events reaches {@code flushEvery}, the buffer is flushed.
     * <p>
     * Any I/O exceptions that occur are passed to the exception handler.
     *
     * @param event the event to write
     * @throws NullPointerException if the event is null
     */
    @Override
    public void accept(LogEvent event) {
        Objects.requireNonNull(event);

        // format outside to prevent locking for too long
        String formatted = formatter.format(event);

        synchronized (this) {
            if (closed) return;

            try {
                writer.write(formatted);
                writer.newLine();
                count++;

                if (count >= flushEvery) {
                    writer.flush();
                    count = 0;
                }
            } catch (IOException e) {
                exceptionHandler.handle(e);
            }
        }
    }

    /**
     * Closes this file sink, flushing any buffered events and closing the file.
     * <p>
     * This method is idempotent and thread-safe. Any I/O exceptions are passed to the exception handler.
     */
    @Override
    public void close() {
        synchronized (this) {
            if (closed) return;
            closed = true;

            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                exceptionHandler.handle(e);
            }
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public String toString() {
        return "FileSink{" +
                "formatter=" + formatter +
                ", exceptionHandler=" + exceptionHandler +
                ", flushEvery=" + flushEvery +
                ", writer=" + writer +
                ", closed=" + closed +
                ", count=" + count +
                '}';
    }
}
