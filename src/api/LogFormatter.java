package api;

import java.time.Instant;

public final class LogFormatter {
    private LogFormatter() {}

    static String formatEvent(LogEvent logEvent) {
        StringBuilder indentBuilder = new StringBuilder();
        StringBuilder logBuilder = new StringBuilder();

        LogPresentation logPresentation = logEvent.logPresentation();
        boolean ansiEnabled = logPresentation.ansiEnabled();

        Instant instant = logEvent.instant();
        String time = logPresentation.timeFormatter().format(instant);

        LogSeverity severity = logEvent.severity();

        indentBuilder.append("[")
                .append(time)
                .append("] [")
                .append(ansiEnabled ? Util.ansiCodeFor(severity) : "")
                .append(severity.name())
                .append(ansiEnabled ? AnsiConstants.RESET : "")
                .append("]");

        String prefix = logEvent.prefix();
        if (prefix != null) {
            indentBuilder.append(" [")
                    .append(prefix)
                    .append("]");
        }

        logBuilder.append(logEvent.log());

        Throwable throwable = logEvent.throwable();
        if (throwable != null) {
            String stackTrace = Util.captureStackTrace(throwable);
            logBuilder.append("\n")
                    .append(stackTrace);
        }

        String indent = indentBuilder.toString();
        String log = logBuilder.toString();

        return Util.formatIndent(log, indent, ansiEnabled);
    }
}
