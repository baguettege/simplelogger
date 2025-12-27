# SimpleLogger

---

## Description

SimpleLogger provides a lightweight, asynchronous and customizable logging utility for Java projects.
Supports multiple log severities, ANSI color output, global prefixing, timestamp formatting, and async logging with a graceful shutdown
mechanism.

---

## Features

- Asynchronous logging via an MPSC pattern - single consumer thread and a queue
- Multiple severity levels: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL`
- Ability to set minimum severity levels to log
- ANSI color support for easy differentiation between log severities
- Customizable timestamp formatting with the `TimeFormatter` interface (default: ISO-8601)
- Global and per-log prefixes
- Thread-safe and non-blocking
- Easy access to a global logger through `LogProvider`
- Implements `AutoClosable` for graceful shutdown

---

## Installation

Requires Java 16+

You can use SimpleLogger in one of two ways:
- **Include in the source code:** copy the `src` folder into your project
- **Include the JAR as an external library:** build SimpleLogger as a JAR and add it to your project

---

## Usage

### Basic Usage

```java
public class Main {
    public static void main(String[] args) {
        try (SimpleLogger logger = new SimpleLogger()) {
            logger.info("Application started");
            logger.warn("This is a warning");
            logger.debug("Debug info");
        } // logger.close() automatically flushes remaining queued logs
    }
}
```

### Using the global logger

```java
public class Main {
    public static void main(String[] args) {
        LogProvider.get().info("Global logger test");
        LogProvider.get().fatal("Program out of memory");
    }
}
```

### Customization

```java
public class Main {
    public static void main(String[] args) {
        SimpleLogger logger = new SimpleLogger()
                .setAnsiEnabled(true)
                .setLevel(SeverityLevel.DEBUG)
                .setPrefixes("Main", "Test")
                .setTimeFormatter(new ISO8601TimeFormatter());
    }
}
```

### Shutdown

Always close the logger by calling `.close();`, or use try-with-resource to ensure all queued logs are flushed and
resources are freed.

```java
logger.close();
```

```java
try (SimpleLogger logger = new SimpleLogger()) {
    // log stuff
}
```

---

## License

This project is licensed under the MIT License.