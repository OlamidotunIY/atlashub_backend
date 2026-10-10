package com.atlashub.pay.charges.infrastructure.messaging.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaystackWebhookReceivedEventTest {

    @Test
    @DisplayName("Should resolve eventType, environment, and data from nested payload")
    void shouldResolveFromNestedPayload() {
        PaystackWebhookReceivedEvent.Payload payload = new PaystackWebhookReceivedEvent.Payload(
                "evt-1",
                "LIVE",
                "charge.success",
                Map.of("reference", "REF-1", "amount", 50000)
        );
        PaystackWebhookReceivedEvent event = new PaystackWebhookReceivedEvent(
                "PaystackWebhookReceivedEvent",
                "evt-1",
                null,
                null,
                payload
        );

        assertEquals("charge.success", event.resolveEventType());
        assertEquals("LIVE", event.resolveEnvironment());
        assertEquals("REF-1", event.resolveData().get("reference"));
        assertEquals(50000, event.resolveData().get("amount"));
    }

    @Test
    @DisplayName("Should resolve from top level properties when payload is null")
    void shouldResolveFromTopLevelWhenPayloadIsNull() {
        PaystackWebhookReceivedEvent event = new PaystackWebhookReceivedEvent(
                "charge.failed",
                "evt-2",
                "TEST",
                Map.of("reference", "REF-2", "gateway_response", "Declined"),
                null
        );

        assertEquals("charge.failed", event.resolveEventType());
        assertEquals("TEST", event.resolveEnvironment());
        assertEquals("Declined", event.resolveData().get("gateway_response"));
    }

    @Test
    @DisplayName("Should return default values when fields are empty or null")
    void shouldReturnDefaultsWhenFieldsAreNull() {
        PaystackWebhookReceivedEvent event = new PaystackWebhookReceivedEvent(
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(null, event.resolveEventType());
        assertEquals("TEST", event.resolveEnvironment());
        assertNotNull(event.resolveData());
        assertTrue(event.resolveData().isEmpty());
    }
}
