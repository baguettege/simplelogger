package api;

/**
 * Represents the severity of a log.
 *
 * <p>
 *     Each enum constant is defined with a priority, defining its importance. A greater number means greater priority.
 * </p>
 */

public enum LogSeverity {
    TRACE(-2),
    DEBUG(-1),
    INFO(0),
    WARN(1),
    ERROR(2),
    FATAL(3);

    private final int priority;

    LogSeverity(int priority) {
        this.priority = priority;
    }

    /**
     * Returns the priority of this enum constant. A greater number means greater priority.
     *
     * @return priority of this enum constant
     */
    public int priority() {
        return priority;
    }
}
