package test;

import impl.logger.SimpleLogger;

public class Main {
    static void main() {
        try (SimpleLogger logger = new SimpleLogger("prefiX", true)) {
            logger.fatal("Hello\nnewline", new AssertionError("DETAILMESSgae"));
        }
    }
}
