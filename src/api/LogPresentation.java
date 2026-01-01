package api;

public record LogPresentation(
        TimeFormatter timeFormatter,
        boolean ansiEnabled
) {
}
