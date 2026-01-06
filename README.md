# SimpleLogger

---

## Description

SimpleLogger is a lightweight, asynchronous and customizable logging utility for Java projects.
Supports multiple log severities, ANSI color output, optional prefixing and async logging with a graceful shutdown.

---

## Features
- Asynchronous logging - Non-blocking logging using a background daemon thread.
- Multiple severity levels - `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL`.
- Customizable output - ANSI colors, prefixes and time formatting.
- Thread-safe - safe to use across multiple threads.
- Extensible - allows for custom `TimeFormatter`s or `AbstractLogger` subclasses.
- Ready-to-use - `ConfigurableLogger` and pre-made `TimeFormatter`s for instant usage.

---

## Installation

Requires Java 16+

You can use SimpleLogger in one of two ways:
- **Include in the source code:** copy the `src` folder into your project
- **Include the JAR as an external library:** build SimpleLogger as a JAR and add it to your project

---

## Usage

### Basic Logging

```java
import impl.logger.ConfigurableLogger;

public class Usage {
    public static void main(String[] args) {
        try (ConfigurableLogger logger = new ConfigurableLogger("Example", true)) {
            logger.info("App startup");
            logger.warn("Warning message");
            logger.error("Error message", new RuntimeException("Example error"));
        }
    }
}
```
- Prefix `Example` will appear in every log
- ANSI color output is enabled with `true`
- Logs automatically flushed when exiting the try-with-resources block

---

### Using Custom Time Formats

```java
import impl.logger.ConfigurableLogger;
import impl.time.SimpleDateTimeFormatter;

public class Usage {
    public static void main(String[] args) {
        ConfigurableLogger logger = new ConfigurableLogger(
                "Example",
                false,
                new SimpleDateTimeFormatter()
        );
        
        logger.info("Custom date-time formatting");
        
        logger.close();
    }
}
```

Available formatters:
- `ISO8601TimeFormatter` - `2026-01-01T00:00:00Z`
- `SimpleTimeFormatter` - `HH:mm:ss`
- `SimpleDateTimeFormatter` - `yyyy-MM-dd HH-mm-ss`

You can also implement your own `TimeFormatter`.

---

### Setting Minimum Severity

```java
import api.LogSeverity;
import impl.logger.ConfigurableLogger;

ConfigurableLogger logger = new ConfigurableLogger();
logger.setMinimum(LogSeverity.WARN); // only WARN and above will be logged
```

---

### Customizability

Time formatting:
- Create a new `TimeFormatter` implementation.
- Use within `LogPresentation`.

Custom loggers:
- Extend `AbstractLogger`.
- Set custom prefixes and `LogPresentation` instances.

---

### Shutdown

Always close the logger using `.close()` or using try-with-resources to flush all queued logs and free resources.

---

## License

This project is licensed under the MIT License.