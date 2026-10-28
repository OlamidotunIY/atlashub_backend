package com.atlashub.integrations.application.port;

public interface WebhookInbox {
    boolean begin(String provider, Long connectionId, String eventId, byte[] payload);
    void markPublished(String provider, Long connectionId, String eventId);
}
