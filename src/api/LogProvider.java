package api;

/**
 * Utility class providing global access to a {@link SimpleLogger} instance.
 */

public final class LogProvider {
    private LogProvider() {}

    /**
     * Global {@link SimpleLogger} instance.
     */
    private static final SimpleLogger GLOBAL_LOGGER = new SimpleLogger();

    /**
     * Returns the global {@link SimpleLogger} instance.
     *
     * @return global {@link SimpleLogger} instance
     */
    public static SimpleLogger get() {
        return GLOBAL_LOGGER;
    }
}
