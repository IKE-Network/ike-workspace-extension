package network.ike.extension;

import java.time.Duration;

/** Short human durations for log lines: {@code 850ms}, {@code 42s}, {@code 3m10s}, {@code 1h05m}. */
final class Durations {

    private Durations() {}

    static String brief(Duration duration) {
        boolean negative = duration.isNegative();
        Duration d = duration.abs();
        String text;
        if (d.toSeconds() < 1) {
            text = d.toMillis() + "ms";
        } else if (d.toMinutes() < 1) {
            long tenths = d.toMillis() / 100;
            text = d.toSeconds() < 10 ? (tenths / 10) + "." + (tenths % 10) + "s" : d.toSeconds() + "s";
        } else if (d.toHours() < 1) {
            text = d.toMinutes() + "m" + String.format("%02ds", d.toSecondsPart());
        } else {
            text = d.toHours() + "h" + String.format("%02dm", d.toMinutesPart());
        }
        return negative ? "-" + text : text;
    }
}
