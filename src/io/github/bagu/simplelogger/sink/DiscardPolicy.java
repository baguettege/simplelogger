package io.github.bagu.simplelogger.sink;

/**
 * Defines policies for handling log events when an {@link AsyncLogSink}'s queue is full.
 *
 * <p>
 *     When the queue reaches capacity, one of these policies determines how the new log events are handled:<br>
 *     - {@link #DROP_NEW} - Discard the new event<br>
 *     - {@link #DROP_OLD} - Remove the oldest queued event to make room for the new one<br>
 *     - {@link #SYNC_FALLBACK} - Process the new event synchronously, bypassing the queue
 * </p>
 */
public enum DiscardPolicy {
    /**
     * Discards new events when the queue is full.
     */
    DROP_NEW,

    /**
     * Discard old events to make room for new ones when the queue is full.
     */
    DROP_OLD,

    /**
     * Process new events synchronously when the queue is full, bypassing the queue.
     */
    SYNC_FALLBACK
}
