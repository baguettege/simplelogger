package impl.logger;

import api.AbstractLogger;
import api.LogPresentation;
import api.TimeFormatter;
import impl.time.ISO8601TimeFormatter;

/**
 * A flexible, ready-to-use logger that allows easy configuration of time formatting, ANSI color output, and optional
 * prefixes.
 *
 * <p>
 *     {@link ConfigurableLogger} is a concrete implementation of {@link AbstractLogger}.
 *     It provides several constructors to easily configure a logger's:
 *     <ul>
 *         <li>Custom time formatters</li>
 *         <li>ANSI color output</li>
 *         <li>Optional prefixes for every message</li>
 *     </ul>
 * </p>
 *
 * <p>
 *     By default, if no time formatter is provided, an {@link ISO8601TimeFormatter} will be used,
 *     ANSI color output is disabled, and no prefix is set ({@code null}).
 * </p>
 */

public final class ConfigurableLogger extends AbstractLogger {

    /**
     * Constructs a new {@link ConfigurableLogger} with a specified prefix, ANSI setting and time formatter.
     *
     * @param prefix optional prefix to log to each message; may be {@code null}
     * @param ansiEnabled whether ANSI color output is enabled or not
     * @param timeFormatter the {@link TimeFormatter} to use for formatting time
     */
    public ConfigurableLogger(String prefix, boolean ansiEnabled, TimeFormatter timeFormatter) {
        super(
                new LogPresentation(
                        timeFormatter,
                        ansiEnabled
                ),
                prefix
        );
    }

    /**
     * Constructs a logger with default {@link ISO8601TimeFormatter}, no prefix, and
     * ANSI color output disabled.
     */
    public ConfigurableLogger() {
        this(null, false, new ISO8601TimeFormatter());
    }

    /**
     * Constructs a logger with default {@link ISO8601TimeFormatter}, a specified prefix, and
     * ANSI color output disabled.
     */
    public ConfigurableLogger(String prefix) {
        this(prefix, false, new ISO8601TimeFormatter());
    }

    /**
     * Constructs a logger with default {@link ISO8601TimeFormatter}, a specified prefix, and
     * ANSI color output setting.
     */
    public ConfigurableLogger(String prefix, boolean ansiEnabled) {
        this(prefix, ansiEnabled, new ISO8601TimeFormatter());
    }

    /**
     * Constructs a logger with a specified {@link TimeFormatter}, no prefix, and
     * ANSI color output disabled.
     */
    public ConfigurableLogger(TimeFormatter timeFormatter) {
        this(null, false, timeFormatter);
    }

    /**
     * Constructs a logger with a specified {@link TimeFormatter}, prefix, and
     * ANSI color output disabled.
     */
    public ConfigurableLogger(String prefix, TimeFormatter timeFormatter) {
        this(prefix, false, timeFormatter);
    }

    /**
     * Constructs a logger with a specified {@link TimeFormatter}, no prefix, and
     * ANSI color output setting.
     */
    public ConfigurableLogger(boolean ansiEnabled, TimeFormatter timeFormatter) {
        this(null, ansiEnabled, timeFormatter);
    }
}
