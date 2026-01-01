package impl.time;

import api.TimeFormatter;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class SimpleTimeFormatter implements TimeFormatter {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String format(Instant instant) {
        LocalTime time = LocalTime.ofInstant(instant, ZoneId.systemDefault());
        return time.format(timeFormatter);
    }
}
