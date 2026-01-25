package io.github.bagu.simplelogger.sink;

import io.github.bagu.simplelogger.Level;
import io.github.bagu.simplelogger.LogEvent;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A log sink that conditionally forwards events to another sink based on a predicate.
 *
 * <p>
 *     This sink acts as a filter, testing each log event against a predicate and only forwarding events
 *     that pass the test to the underlying sink. This is useful for:<br>
 *     - Filtering by log level<br>
 *     - Filtering by logger name for package<br>
 *     - Filtering by message content<br>
 *     - Custom filtering logic
 * </p>
 *
 * <p>
 *     This class is thread-safe as long as the underlying sink and predicate are thread-safe.
 * </p>
 *
 * <pre>{@code
 * // filter to only process ERROR and FATAl events
 * LogSink sink = FilterLogSink.filterByMinLevel(sink, Level.ERROR);
 *
 * // filter to only process events from a specific package
 * LogSink sink = FilterLogSink.filterBy(
 *     sink,
 *     event -> event.loggerName().startsWith("com.myapp.security")
 * );
 * }</pre>
 */
public final class FilterLogSink implements LogSink {
    private final LogSink sink;
    private final Predicate<LogEvent> predicate;

    /**
     * Creates a new filter sink with a custom predicate.
     *
     * @param sink the underlying sink to forward matching events to
     * @param predicate the predicate to test events against
     */
    public FilterLogSink(LogSink sink, Predicate<LogEvent> predicate) {
        this.sink = Objects.requireNonNull(sink);
        this.predicate = Objects.requireNonNull(predicate);
    }

    @Override
    public void accept(LogEvent event) {
        if (predicate.test(event))
            sink.accept(event);
    }

    @Override
    public void close() {
        this.sink.close();
    }

    @Override
    public boolean isClosed() {
        return this.sink.isClosed();
    }

    /**
     * Creates a filter that only accepts events at or above the specified minimum level.
     *
     * @param sink the underlying sink
     * @param minLevel the minimum level to accept (inclusive)
     * @return a new filter sink
     */
    public static FilterLogSink filterByMinLevel(LogSink sink, Level minLevel) {
        return new FilterLogSink(
                sink,
                event -> event.level().ordinal() >= minLevel.ordinal()
        );
    }

    /**
     * Creates a filter that only accepts events at or below the specified maximum level.
     *
     * @param sink the underlying sink
     * @param maxLevel the maximum level to accept (inclusive)
     * @return a new filter sink
     */
    public static FilterLogSink filterByMaxLevel(LogSink sink, Level maxLevel) {
        return new FilterLogSink(
                sink,
                event -> event.level().ordinal() <= maxLevel.ordinal()
        );
    }

    /**
     * Creates a filter that only accepts events within the specified level range.
     *
     * @param sink the underlying sink
     * @param minLevel the minimum level to accept (inclusive)
     * @param maxLevel the maximum level to accept (inclusive)
     * @return a new filter sink
     */
    public static FilterLogSink filterByLevelRange(LogSink sink, Level minLevel, Level maxLevel) {
        return new FilterLogSink(
                sink,
                event -> event.level().ordinal() >= minLevel.ordinal() &&
                        event.level().ordinal() <= maxLevel.ordinal()
        );
    }

    /**
     * Creates a filter that only accepts events matching specific levels.
     *
     * @param sink the underlying sink
     * @param levels the levels to accept
     * @return a new filter sink
     */
    public static FilterLogSink filterByLevels(LogSink sink, Level... levels) {
        Set<Level> levelSet = Set.of(levels);
        return new FilterLogSink(
                sink,
                event -> levelSet.contains(event.level())
        );
    }

    /**
     * Creates a filter with a custom predicate.
     *
     * <p>
     *     This is an alias for the constructor, provided for consistency with other factory methods.
     * </p>
     *
     * @param sink the underlying sink
     * @param predicate the predicate to test events against
     * @return a new filter sink
     */
    public static FilterLogSink filterBy(LogSink sink, Predicate<LogEvent> predicate) {
        return new FilterLogSink(
                sink,
                predicate
        );
    }
}
