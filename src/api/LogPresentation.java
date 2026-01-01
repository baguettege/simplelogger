package api;

/**
 * Stores information on how to present logs.
 *
 * @param timeFormatter timestamp formatter for logs
 * @param ansiEnabled whether ANSI color output is enabled or not
 */

public record LogPresentation(
        TimeFormatter timeFormatter,
        boolean ansiEnabled
) {
}
