package io.github.bagu.simplelogger.sink;

import io.github.bagu.simplelogger.LogEvent;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An asynchronous log sink that processes log events in a background thread.
 *
 * <p>
 *     This sink queues log events and processes them asynchronously to avoid blocking the logging thread.
 *     When the queue is full, the behaviour is determined by the configured {@link DiscardPolicy}:<br>
 *     - {@link DiscardPolicy#DROP_NEW} - Discard new events<br>
 *     - {@link DiscardPolicy#DROP_OLD} - Discard old events<br>
 *     - {@link DiscardPolicy#SYNC_FALLBACK} - Process events synchronously
 * </p>
 *
 * <p>
 *     A shutdown hook is registered to ensure all queued events are processed during JVM shutdown.
 * </p>
 *
 * <p>
 *     This class is fully thread-safe. All operations are synchronized appropriately.
 * </p>
 *
 * <pre>{@code
 * AsyncLogSink asyncSink = AsyncLogSink.builder()
 *     .sink(sink)
 *     .policy(DiscardPolicy.DROP_OLD)
 *     .queueSize(2048)
 *     .isDaemon(true)
 *     .build();
 *
 * // shutdown
 * asyncSink.close();
 *
 * // check for dropped events
 * long dropped = asyncSink.getDroppedCount();
 * }</pre>
 */
public final class AsyncLogSink implements LogSink {
    private static final int POLL_TIMEOUT_MILLIS = 100;

    private final Thread shutdownHook;
    private final Object lock = new Object();
    private volatile boolean isClosed = false;
    private volatile long droppedCount = 0;
    private final Thread workerThread;
    private final BlockingQueue<LogEvent> queue;

    private final LogSink sink;
    private final DiscardPolicy policy;

    /**
     * Creates a new asynchronous log sink.
     *
     * <p>
     *     It is recommended to use the {@link Builder} via {@link #builder()} instead of calling
     *     this constructor directly.
     * </p>
     *
     * @param sink the underlying sink to which events will be written
     * @param policy the policy for handling queue overflow
     * @param queueSize the maximum number of events that can be queued, must be > 0
     * @param isDaemon {@code true} to run the worker thread as a daemon, otherwise {@code false}
     * @throws IllegalArgumentException if the queue size < 1
     */
    public AsyncLogSink(
            LogSink sink,
            DiscardPolicy policy,
            int queueSize,
            boolean isDaemon
    ) {
        if (queueSize < 1)
            throw new IllegalArgumentException("Queue size < 1:" + queueSize);

        this.sink = Objects.requireNonNull(sink);
        this.policy = Objects.requireNonNull(policy);
        this.queue = new LinkedBlockingQueue<>(queueSize);

        this.shutdownHook = new Thread(this::close);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        this.workerThread = new Thread(this::processLoop, "Async-sink-" + System.identityHashCode(this));
        this.workerThread.setDaemon(isDaemon);
        this.workerThread.start();
    }

    /**
     * The method ran on the designated worker thread for handling events asynchronously.
     */
    private void processLoop() {
        try {
            while (!isClosed || !queue.isEmpty()) {
                LogEvent event = queue.poll(POLL_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                if (event != null) {
                    synchronized (lock) {
                        sink.accept(event);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            LogEvent event;
            synchronized (lock) {
                while ((event = queue.poll()) != null)
                    sink.accept(event);
            }
        }
    }

    /**
     * Returns the number of log events that have been dropped due to queue overflow.
     *
     * <p>
     *     This count only increases when using {@link DiscardPolicy#DROP_NEW} or
     *     {@link DiscardPolicy#DROP_OLD} policies.
     * </p>
     *
     * @return the total number of dropped events since this sink was created
     */
    public long getDroppedCount() {
        return droppedCount;
    }

    @Override
    public void accept(LogEvent event) {
        synchronized (lock) {
            if (isClosed) return;

            if (!queue.offer(event)) {
                switch (policy) {
                    case DROP_NEW:
                        droppedCount++;
                        break;
                    case DROP_OLD:
                        while (!queue.offer(event)) {
                            LogEvent droppedEvent = queue.poll();
                            if (droppedEvent != null)
                                droppedCount++;
                        }
                        break;
                    case SYNC_FALLBACK:
                        sink.accept(event);
                        break;
                    default:
                        throw new AssertionError("Unaccounted discard policy: " + policy);
                }
            }
        }
    }

    /**
     * Closes this sink, processes all remaining queued events, and releases resources.
     *
     * <p>
     *     This method can be called multiple times safely.
     * </p>
     */
    @Override
    public void close() {
        synchronized (lock) {
            if (isClosed) return;
            isClosed = true;

            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {}

            workerThread.interrupt();
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            this.sink.close();
        }
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }

    /**
     * Creates a new builder for constructing an {@code AsyncLogSink}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating {@link AsyncLogSink} instances.
     *
     * <p>
     *     Default values:<br>
     *     - sink: none, required to build
     *     - policy: {@link DiscardPolicy#DROP_NEW}<br>
     *     - queueSize: {@code 1024}<br>
     *     - isDaemon: {@code true}
     * </p>
     *
     */
    public static final class Builder {
        private LogSink sink;
        private DiscardPolicy policy = DiscardPolicy.DROP_NEW;
        private int queueSize = 1024;
        private boolean isDaemon = true;

        /**
         * Builds the {@link AsyncLogSink} with the configured parameters.
         *
         * @return a new {@link AsyncLogSink} instance
         * @throws NullPointerException if the sink has not been set
         */
        public AsyncLogSink build() {
            return new AsyncLogSink(
                    Objects.requireNonNull(sink),
                    policy,
                    queueSize,
                    isDaemon
            );
        }

        /**
         * Sets the underlying sink to which events will be written.
         *
         * @param sink the sink
         * @return this builder
         */
        public Builder sink(LogSink sink) {
            this.sink = Objects.requireNonNull(sink);
            return this;
        }

        /**
         * Sets the policy for handling queue overflow.
         *
         * @param policy the discard policy
         * @return this builder
         */
        public Builder policy(DiscardPolicy policy) {
            this.policy = Objects.requireNonNull(policy);
            return this;
        }

        /**
         * Sets the maximum queue size.
         *
         * @param queueSize the queue size, must be at least 1
         * @return this builder
         * @throws IllegalArgumentException if the queue size is < 1
         */
        public Builder queueSize(int queueSize) {
            if (queueSize < 1)
                throw new IllegalArgumentException("Queue size < 1:" + queueSize);
            this.queueSize = queueSize;
            return this;
        }

        /**
         * Sets whether the worker thread should be a daemon thread.
         *
         * @param isDaemon {@code true} for daemon thread, {@code false} otherwise
         * @return this builder
         */
        public Builder isDaemon(boolean isDaemon) {
            this.isDaemon = isDaemon;
            return this;
        }
    }
}
