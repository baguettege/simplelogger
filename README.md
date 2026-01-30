# SimpleLogger

A lightweight, flexible Java logging library with a focus on simplicity and performance.

---

# Features

- **Simple API** - Intuitive logger interface with standard log levels (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
- **Flexible Sinks** - Write logs to console, files, or custom destinations
- **Async Support** - Non-blocking logging with configurable async sinks
- **Parameterized Messages** - Efficient message formatting with `{}` placeholders
- **Thread-safe** - All components designed for concurrent use
- **Zero Dependencies** - Pure Java with no external dependencies
- **Composable** - Chain and filter sinks to build complex logging pipelines

---

# Why SimpleLogger?

SimpleLogger is designed for applications that need:
- A small, dependency free logging core
- Predictable, thread-safe behavior
- Control over log destinations
- Safe logging without static state

---

# Installation

Download the latest release from the [releases page](https://github.com/baguettege/simplelogger/releases)
and add it to your project's classpath.

Alternatively, you can include the `src` directory directly in your project.

---

# Quick Start

```java
// console logger
Logger logger = new Logger(
        "MyApp",
        Level.INFO,
        new ConsoleSink(new LogFormatter(
                new ISO8601TimeFormatter(),
                LogField.TIMESTAMP,
                LogField.LEVEL,
                LogField.THREAD_NAME,
                LogField.LOGGER_NAME
        ))
);

logger.info("App started");
logger.warn("Failed to connect to {}", host);
logger.fatal("Low memory: {}", availableMemory);
```

---

# Usage Guide

### Creating a `Logger`

Every logger requires a name and a sink:
```java
Logger logger = new Logger("MyApp", sink);
```

Optionally specify a minimum log level:
```java
// ignore TRACE and DEBUG logs
Logger logger = new Logger("MyApp", Level.INFO, sink);
```

### Log Levels

From lowest to highest severity:

- `TRACE` - Fine-grained debugging information
- `DEBUG` - Detailed debugging information
- `INFO` - General information messages
- `WARN` - Warning messages for potentially harmful situations
- `ERROR` - Error events that might still allow the app to continue
- `FATAL` - Severe errors that will likely cause termination

### Message Formatting

Use `{}` as placeholders for parameters:
```java
logger.info("User {} logged in from {}", username, address);
// output: "User Alice logged in from 127.0.0.1"
```

Escape braces with a backslash:

```java
logger.info("Use \\{} for placeholders");
// output: "Use {} for placeholders"
```

Literal `\` + parameter:

```java
logger.info("\\\\{}", 1);
// output: "\1"
```

### Exception Logging

Pass a `Throwable` as the last parameter to automatically include the stack trace:

```java
try {
    new Socket("localhost", 443);
} catch (IOException e) {
    logger.error("Failed to connect", e);
}
```

## Sinks

### `ConsoleSink`

Writes to `System.out` (TRACE through WARN) or `System.err` (ERROR and FATAL):

```java
LogSink sink = new ConsoleSink(logFormatter);
```

### `FileSink`

Writes to a file with configurable buffering:

```java
LogSink sink = new FileSink(
        Path.of("session.log"),
        e -> System.err.println("Log error: " + e),
        10,
        logFormatter
);
```

### `CompositeSink`

Write to multiple destinations simultaneously:

```java
LogSink sink = new CompositeSink(
        consoleSink,
        fileSink
);
```

### `FilteredSink`

Filter events by level, logger name, or custom predicates:

```java
// only log ERROR and FATAL
LogSink errorSink = new FilteredSink(
        sink,
        event -> event.level().ordinal() >= Level.ERROR.ordinal()
);

// only log from specific logger
LogSink securitySink = new FilteredSink(
        sink,
        event -> event.loggerName().equals("Security")
);
```

### `NullSink`

Discards all events:

```java
LogSink sink = new NullSink();
```

## Async Sinks

### `ReliableAsyncSink`

Guarantees all events are written, but uses unbounded memory:

```java
LogSink sink = new ReliableAsyncSink(otherSink, 2); // 2 worker threads
```

### `LossyAsyncSink`

Uses bounded queue and may drop new events under heavy load:

```java
LogSink sink = new LossyAsyncSink(otherSink, 1024); // 1024 queue size
long dropped = sink.getDroppedCount(); // check drops
```

## Custom `LogSink`

You can create custom sinks to allow for many different log destinations:

```java
public final class CustomSink implements LogSink {
    @Override
    public void accept(LogEvent event) {
        // logic
    }
    
    @Override
    public void close() {
        // logic
    }
    
    @Override
    public boolean isClosed() {
        // logic
        return ?;
    }
}
```

## Formatters

### `LogFormatter`

Configure which metadata fields appear in log prefixes:

```java
LogFormatter detailFormatter = new LogFormatter(
        timeFormatter,
        LogField.TIMESTAMP,
        LogField.LEVEL,
        LogField.THREAD_NAME,
        LogField.LOGGER_NAME
);
// output: [2024-01-28 14:30:45] [INFO] [main] [MyApp] Application started

LogFormatter simpleFormatter = new LogFormatter(
        timeFormatter,
        LogField.TIMESTAMP,
        LogField.LEVEL
);
// output: [2024-01-28 14:30:45] [INFO] Application started
```

### `TimeFormatter`

ISO-8601:

```java
TimeFormatter formatter = new ISO8601TimeFormatter();
// output: 2024-01-28T14:30:45.123Z
```

Local Date-Time:

```java
TimeFormatter formatter = new LocalDateTimeFormatter();
// output: 2024-01-28 14:30:45
```

Local Time:

```java
TimeFormatter formatter = new LocalTimeFormatter();
// output: 14:30:45
```

#### Custom time formatting:

`TimeFormatter` is a functional interface, so custom time formatting can be implemented using
a lambda function, method reference, or a class implementing `TimeFormatter`.

---

## Best Practices

Always close loggers when done to flush buffered events and release resources:

```java
try (Logger logger = new Logger("MyApp", sink)) {
    logger.info(...);
    // logic...
} // automatically closed (closes underlying sink)
```

All sinks are designed to be thread-safe and non-throwing, breaking this may cause unexpected
outcomes.

---

## Architecture

```
Logger
 └─> LogEvent
     └─> LogSink
          ├─> ConsoleSink
          ├─> FileSink
          ├─> CompositeSink
          ├─> FilteredSink
          ├─> ReliableAsyncSink
          ├─> LossyAsyncSink
          └─> NullSink
          
LogFormatter
 ├─> TimeFormatter
 │    ├─> ISO8601TimeFormatter
 │    ├─> LocalDateTimeFormatter
 │    └─> LocalTimeFormatter
 └─> LogField
```

---

## Requirements

- Built for Java 17+, but may support lower levels. Check release information for specifics
- No external dependencies

---

## License

- This project is licensed under the MIT License.