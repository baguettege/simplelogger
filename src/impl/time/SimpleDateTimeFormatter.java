package impl.time;

import api.TimeFormatter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class SimpleDateTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");

    @Override
    public String format(Instant instant) {
        LocalDateTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return time.format(timeFormatter);
    }
}
