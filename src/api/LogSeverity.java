package api;

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

    public int priority() {
        return priority;
    }
}
