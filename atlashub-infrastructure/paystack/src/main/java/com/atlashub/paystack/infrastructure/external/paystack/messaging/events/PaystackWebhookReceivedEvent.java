package com.atlashub.paystack.infrastructure.external.paystack.messaging.events;

import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;

import java.util.Map;

public record PaystackWebhookReceivedEvent(String eventId, PaystackEnvironment environment, String eventType,
                                           Map<String, Object> data) {
    public PaystackWebhookReceivedEvent {
        if (eventId == null || eventId.isBlank() || eventType == null || eventType.isBlank())
            throw new IllegalArgumentException("Paystack webhook event identity is required");
        if (environment == null) throw new IllegalArgumentException("Paystack environment is required");
        data = data == null ? Map.of() : Map.copyOf(data);
    }
}
