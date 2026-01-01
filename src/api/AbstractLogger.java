package api;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractLogger implements AutoCloseable {
    private final Thread logThread;
    private final LinkedBlockingQueue<LogEvent> logQueue = new LinkedBlockingQueue<>();
    private volatile LogSeverity minimum = LogSeverity.INFO;

    protected AbstractLogger() {
        logThread = new Thread(this::logLoop, "Log-thread-" + System.identityHashCode(this));
        logThread.setDaemon(true);
        logThread.start();
    }

    public final void setMinimum(LogSeverity severity) {
        this.minimum = severity;
    }

    protected final void queueLog(LogEvent logEvent) {
        LogSeverity severity = logEvent.severity();
        if (severity.priority() < minimum.priority()) return;

        logQueue.offer(logEvent);
    }

    private void logLoop() {
        try {
            while (true) {
                LogEvent logEvent = logQueue.take();
                logDirect(logEvent);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            LogEvent logEvent;
            while ((logEvent = logQueue.poll()) != null) {
                logDirect(logEvent);
            }
        }
    }

    private void logDirect(LogEvent logEvent) {
        String formatted = LogFormatter.formatEvent(logEvent);
        LogSeverity severity = logEvent.severity();

        if (severity == LogSeverity.ERROR || severity == LogSeverity.FATAL) {
            System.err.println(formatted);
            System.err.flush();
        } else {
            System.out.println(formatted);
        }
    }

    @Override
    public final void close() {
        try {
            logThread.interrupt();
            logThread.join();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    public abstract void trace(String log);
    public abstract void debug(String log);
    public abstract void info(String log);
    public abstract void warn(String log);
    public abstract void error(String log);
    public abstract void fatal(String log);

    public abstract void debug(String log, Throwable throwable);
    public abstract void error(String log, Throwable throwable);
    public abstract void fatal(String log, Throwable throwable);
}
