# SimpleLogger

A lightweight, zero-dependency Java logging library focused on clarity, composability and thread-safety.

SimpleLogger is designed to be easy to embed in small projects or libraries where heavyweight logging
frameworks are undesirable, while still providing powerful building blocks such as asynchronous
logging, filtering, formatting and sink composition.

---

## Features

- **Zero external dependencies**
- **Fully thread-safe** core and sinks
- **Composable sinks** (console, async, filtered, composite)
- **Structured log events** with rich context
- **Asynchronous logging** with configurable backpressure policies
- **Flexible filtering** by level or custom predicates
- **AutoCloseable loggers & sinks** (try-with-resources friendly)
- **Pluggable time and message formatting**
- **Automatic exception stack trace rendering**

---

## Why SimpleLogger?

SimpleLogger is designed for applications that need:
- A small, dependency-free logging core
- Predictable, thread-safe behaviour
- Control over sinks, formatting and filtering
- Safe usage in libraries without static state

---

## Installation

- **Include in the source code:** copy the `src` folder into your project
- **Include the JAR as an external library:** download a release JAR and include in your project

---

## Quick Start

```java
LogFormatter logFormatter = new DefaultLogFormatter(
        new SimpleTimeFormatter(),
        true // show thread names
);

LogSink sink = new PrintStreamLogSink(System.out, logFormatter);

try (CloseableLogger logger = new SimpleLogger("MyApp", sink)) {
    logger.info("Application started on port {}", 8080);
    logger.warn("High memory usage: {}%", 92);
}
```

---

## Logging Levels

Supported levels, in ascending order of severity:

`TRACE` > `DEBUG` > `INFO` > `WARN` > `ERROR` > `FATAL`

Level ordering allows easy filtering using ordinal comparisons.

---

## Message Formatting

Messages use `{}` placeholders:

```java
logger.debug("User {} logged in from {}", "alice", "192.168.0.1");
```

### Escaping rules:

- `{}` > parameter value
- `\{}` > `{}`
- `\\` > `\`
- extra `{}` > left unchanged (`{}`)
- extra parameters > ignored

### Exceptions

If the last parameter is a `Throwable`, its stack trace is automatically appended:

```java
logger.error("Failed to connect to server", new IOException());
```

---

## Log Sinks

Sinks define where logs go.

### PrintStreamLogSink

Writes formatted logs to a single `PrintStream`.

```java
LogSink sink = new PrintStreamLogSink(
        System.out,
        formatter
);
```

### DualPrintStreamLogSink

Routes logs by severity:
- `ERROR`, `FATAL` > `System.err`
- all others > `System.out`

```java
LogSink sink = new DualPrintStreamLogSink(
        System.out,
        System.err,
        formatter
);
```

### CompositeLogSink

Branch out logs to multiple sinks:

```java
LogSink sink = new CompositeLogSink(
        consoleSink,
        fileSink,
        databaseSink
);
```

### FilterLogSink

Filter events before they reach a sink:

```java
LogSink errorOnlySink = FilterLogSink.filterByMinLevel(
    sink,
    Level.ERROR
);
```

Custom predicates are also supported:

```java
LogSink authOnlySink = FilterLogSink.filterBy(
    sink,
    event -> event.loggerName().startsWith("Auth")
);
```

### AsyncLogSink

Process logs asynchronously using a background thread.

```java
LogSink asyncSink = AsyncLogSink.builder()
    .sink(sink)
    .queueSize(2048)
    .policy(DiscardPolicy.DROP_OLD)
    .isDaemon(true)
    .build();
```

Discard Policies:
- `DROP_NEW` > drop new events
- `DROP_OLD` > drop oldest queued event
- `SYNC_FALLBACK` > log synchronously

Dropped event count is tracked:

```java
long dropped = asyncSink.getDroppedCount();
```

---

## Formatters

### DefaultLogFormatter

Produces aligned, multi-line output:

```
[14:30:15] [INFO] [main] [MyApp] Application started
                                 Listening on port 8080
```

Supports:
- multi-line indentation
- exception stack traces
- optional thread names

### Time Formatters

Included implementations:
- `SimpleTimeFormatter` > `HH:mm:ss`
- `SimpleDateTimeFormatter` > `yyyy-MM-dd HH:mm:ss`
- `ISO8601TimeFormatter` > ISO-8601 timestamps

Custom implementations can be plugged in via `TimeFormatter`.

---

## Architecture Overview

```
Logger
  ↓
LogEvent
  ↓
LogSink
   ├─ FilterLogSink
   ├─ AsyncLogSink
   ├─ CompositeLogSink
   ├─ Output Sink
   └─ Custom Sink
```

### Thread-Safety

- all loggers, sinks and formatters are thread-safe.
- async sinks synchronize access to underlying sinks
- close operations can be called multiple times safely

### Design Philosophy

- Keep APIs small and predictable
- Make logging safe in library code
- Never block application threads unless explicitly configured

---

## Requirements

- Java 11+
- No external dependencies

---

## License

- This project is licensed under the MIT License.