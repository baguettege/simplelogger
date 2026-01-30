package io.github.bagu.simplelogger.sinks;

import io.github.bagu.simplelogger.LogEvent;
import io.github.bagu.simplelogger.format.LogFormatter;
import io.github.bagu.simplelogger.LogSink;

import java.util.Objects;

/**
 * A log sink that writes formatted log events to the console.
 * <p>
 * Events at {@link io.github.bagu.simplelogger.Level#ERROR} and
 * {@link io.github.bagu.simplelogger.Level#FATAL} levels are written to {@link System#err},
 * while all other levels are written to {@link System#out}.
 * <p>
 * Note that this sink never reports as closed and does not close the standard streams.
 *
 * @see LogSink
 * @see LogFormatter
 */
public final class ConsoleSink implements LogSink {
    private final LogFormatter formatter;

    /**
     * Constructs a console sink with the specified formatter.
     *
     * @param formatter the formatter to use for rendering log events
     * @throws NullPointerException if the formatter is null
     */
    public ConsoleSink(LogFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter);
    }

    /**
     * Accepts a log event, formats it, and writes it to the appropriate console stream.
     * <p>
     * Events at {@link io.github.bagu.simplelogger.Level#ERROR} and {@link io.github.bagu.simplelogger.Level#FATAL}
     * are written to {@link System#err}, while all other levels are written to {@link System#out}.
     *
     * @param event the event to write
     * @throws NullPointerException if event is {@code null}
     */
    @Override
    public void accept(LogEvent event) {
        Objects.requireNonNull(event);

        String formatted = formatter.format(event);

        switch (event.level()) {
            case ERROR, FATAL -> System.err.println(formatted);
            default -> System.out.println(formatted);
        }
    }

    /**
     * No-op method. Console streams are not closed.
     */
    @Override
    public void close() {
        // no op - do not want to close stdout/stderr
    }

    /**
     * Always returns {@code false} as console streams are never closed.
     *
     * @return {@code false}
     */
    @Override
    public boolean isClosed() {
        // stdout/stderr is always open
        return false;
    }

    @Override
    public String toString() {
        return "ConsoleSink{" +
                "formatter=" + formatter +
                '}';
    }
}
