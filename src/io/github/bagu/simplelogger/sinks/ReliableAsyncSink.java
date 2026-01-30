package io.github.bagu.simplelogger.sinks;

import io.github.bagu.simplelogger.LogEvent;
import io.github.bagu.simplelogger.LogSink;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * An asynchronous log sink that processes events using a thread pool.
 * <p>
 * This sink guarantees that all log events will be eventually written (unless the JVM
 * terminates unexpectedly), but may cause memory issues or blocking if events are
 * produced faster than they can be processed, as it uses an unbounded queue.
 * <p>
 * Notes:
 * <ol>
 * <li>Uses an unbounded queue, which may result in a {@link OutOfMemoryError}</li>
 * <li>Uses non-daemon threads, preventing JVM shutdown until all events are processed</li>
 * <li>Guarantees event delivery if {@link #close()} completes successfully</li>
 * </ol>
 * <p>
 * For a bounded alternative that may drop events, see {@link LossyAsyncSink}.
 *
 * @see LogSink
 * @see LossyAsyncSink
 */
public final class ReliableAsyncSink implements LogSink {
    private final LogSink sink;
    private final ExecutorService service;

    private volatile boolean closed = false;

    /**
     * Constructs a reliable async sink with the specified underlying sink and thread count.
     *
     * @param sink the underlying sink to write events to
     * @param nThreads the number of worker threads to use for processing events
     * @throws NullPointerException if the sink is null
     * @throws IllegalArgumentException if nThreads is less than 1
     */
    public ReliableAsyncSink(LogSink sink, int nThreads) {
        // unbounded so may cause OOM
        // uses non-daemon threads so will prevent JVM shutdown
        this.sink = Objects.requireNonNull(sink);
        if (nThreads < 1) {
            throw new IllegalArgumentException("Thread count < 1: " + nThreads);
        }
        this.service = Executors.newFixedThreadPool(nThreads);
    }

    /**
     * Accepts a log event and queues it for asynchronous processing.
     * <p>
     * If this sink is closed, the event is silently dropped.
     *
     * @param event the event to process
     * @throws NullPointerException if the event is null
     */
    @Override
    public void accept(LogEvent event) {
        Objects.requireNonNull(event);

        if (closed) {
            return;
        }

        try {
            service.execute(() -> sink.accept(event));
        } catch (RejectedExecutionException ignored) {
            // closed, may drop logs
        }
    }

    /**
     * Closes this sink, waiting for pending events to be processed.
     * <p>
     * This method:
     * <ol>
     * <li>Stops accepting new events</li>
     * <li>Initiates an orderly shutdown of the thread pool</li>
     * <li>Waits up to 5 seconds for pending events to complete</li>
     * <li>Forces shutdown if timeout is exceeded</li>
     * <li>Closes the underlying sink</li>
     * </ol>
     * <p>
     * This method is idempotent and thread-safe.
     */
    @Override
    public void close() {
        synchronized (this) {
            if (closed) return;
            closed = true;

            service.shutdown();

            try {
                if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                    service.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                service.shutdownNow();
            }

            sink.close();
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public String toString() {
        return "ReliableAsyncSink{" +
                "sink=" + sink +
                ", service=" + service +
                ", closed=" + closed +
                '}';
    }
}
