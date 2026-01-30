package io.github.bagu.simplelogger.sinks;

import io.github.bagu.simplelogger.LogEvent;
import io.github.bagu.simplelogger.LogSink;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An asynchronous log sink that uses a bounded queue and may drop events under load.
 * <p>
 * This sink processes events on a dedicated background thread. If the queue is full
 * when a new event arrives, that event is dropped and the drop counter is incremented.
 * This prevents unbounded memory growth but may result in log loss during traffic spikes.
 * <p>
 * Notes:
 * <ol>
 * <li>Uses a bounded queue to prevent {@link OutOfMemoryError}</li>
 * <li>May drop events if the queue is full (use {@link #getDroppedCount()} to check)</li>
 * <li>Processes remaining queued events on close</li>
 * <li>Uses a single daemon thread</li>
 * </ol>
 * <p>
 * For a guaranteed delivery alternative, see {@link ReliableAsyncSink}.
 *
 * @see LogSink
 * @see ReliableAsyncSink
 */
public final class LossyAsyncSink implements LogSink {
    private final LogSink sink;

    private final ArrayBlockingQueue<LogEvent> queue;
    private final Thread workerThread;
    private volatile boolean closed = false;
    private final AtomicLong dropped = new AtomicLong(0);

    /**
     * Constructs a lossy async sink with the specified underlying sink and queue size.
     *
     * @param sink the underlying sink to write events to
     * @param queueSize the maximum number of events that can be queued
     * @throws NullPointerException if sink is null
     * @throws IllegalArgumentException if queueSize is less than 1
     */
    public LossyAsyncSink(LogSink sink, int queueSize) {
        if (queueSize < 1) {
            throw new IllegalArgumentException("Queue size < 1: " + queueSize);
        }

        this.sink = Objects.requireNonNull(sink);
        this.queue = new ArrayBlockingQueue<>(queueSize);
        this.workerThread = new Thread(this::loop, "Async-sink-" + System.identityHashCode(this));
        this.workerThread.start();
    }

    /**
     * The main processing loop that runs on the worker thread.
     * <p>
     * This method continuously takes events from the queue and forwards them to
     * the underlying sink until the sink is closed. When closing, it processes
     * all remaining events in the queue.
     */
    private void loop() {
        try {
            while (!closed) {
                LogEvent event = queue.take();
                sink.accept(event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            LogEvent event;
            while ((event = queue.poll()) != null) {
                sink.accept(event);
            }
        }
    }

    /**
     * Accepts a log event and queues it for asynchronous processing.
     * <p>
     * If the queue is full or the sink is closed, the event is dropped and
     * the drop counter is incremented.
     *
     * @param event the event to queue
     * @throws NullPointerException if event is null
     */
    @Override
    public void accept(LogEvent event) {
        Objects.requireNonNull(event);
        if (closed || !queue.offer(event)) {
            dropped.incrementAndGet();
        }
    }

    /**
     * Closes this async sink, processing all remaining queued events.
     * <p>
     * This method:
     * <ol>
     * <li>Stops accepting new events</li>
     * <li>Interrupts the worker thread</li>
     * <li>Waits for the worker thread to finish processing queued events</li>
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

            workerThread.interrupt();
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            sink.close();
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * Returns the number of log events that this sink has dropped.
     *
     * @return number of dropped log events so far
     */
    public long getDroppedCount() {
        return dropped.get();
    }

    @Override
    public String toString() {
        return "LossyAsyncSink{" +
                "sink=" + sink +
                ", queue=" + queue +
                ", workerThread=" + workerThread +
                ", closed=" + closed +
                ", dropped=" + dropped +
                '}';
    }
}
