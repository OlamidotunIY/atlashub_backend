package com.atlashub.shared.api;

public interface EventTrackerApi {
    /**
     * Registers a consumer for a specific event type.
     * @param eventType the class simple name of the event (e.g., AdminCreatedEvent)
     * @param consumerId the Kafka group id or unique consumer identifier
     */
    void registerSubscription(String eventType, String consumerId);

    /**
     * Marks an event as successfully processed by a consumer.
     */
    void markSuccess(String eventId, String consumerId);

    /**
     * Marks an event as failed (dead lettered) for a consumer.
     */
    void markDlq(String eventId, String consumerId);

    /**
     * Checks if an event is already processed (or dead lettered) by a consumer.
     * Used for idempotency.
     */
    boolean isProcessed(String eventId, String consumerId);
}
