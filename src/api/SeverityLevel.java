package api;

/**
 * Represents the severity of a log message.
 * <p>
 *     Their integer values are used to compare the importance of severities, allowing for
 *     the setting of minimum severity logs within a {@link SimpleLogger}.
 * </p>
 */

public enum SeverityLevel {
    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    FATAL(5);

    private final int value;

    SeverityLevel(int value) {
        this.value = value;
    }

    /**
     * Returns the priority value for this severity level.
     * <p>
     *     Higher values means higher priority.
     * </p>
     *
     * @return priority value
     */
    public int priority() {
        return value;
    }
}
