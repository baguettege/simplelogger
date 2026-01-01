package impl.logger;

import api.*;
import impl.time.SimpleTimeFormatter;

import java.time.Instant;

public final class SimpleLogger extends AbstractLogger {
    private final LogPresentation logPresentation;
    private final String prefix;

    public SimpleLogger(String prefix, boolean ansiEnabled) {
        this.prefix = prefix;
        this.logPresentation = new LogPresentation(
                new SimpleTimeFormatter(),
                ansiEnabled
        );
    }

    private void log(LogSeverity severity, String log, Throwable throwable) {
        LogEvent logEvent = new LogEvent(
                Instant.now(),
                severity,
                prefix,
                log,
                throwable,
                logPresentation
        );

        super.queueLog(logEvent);
    }

    @Override
    public void trace(String log) {
        this.log(LogSeverity.TRACE, log, null);
    }

    @Override
    public void debug(String log) {
        this.log(LogSeverity.DEBUG, log, null);
    }

    @Override
    public void info(String log) {
        this.log(LogSeverity.INFO, log, null);
    }

    @Override
    public void warn(String log) {
        this.log(LogSeverity.WARN, log, null);
    }

    @Override
    public void error(String log) {
        this.log(LogSeverity.ERROR, log, null);
    }

    @Override
    public void fatal(String log) {
        this.log(LogSeverity.FATAL, log, null);
    }

    @Override
    public void debug(String log, Throwable throwable) {
        this.log(LogSeverity.DEBUG, log, throwable);
    }

    @Override
    public void error(String log, Throwable throwable) {
        this.log(LogSeverity.ERROR, log, throwable);
    }

    @Override
    public void fatal(String log, Throwable throwable) {
        this.log(LogSeverity.FATAL, log, throwable);
    }
}
