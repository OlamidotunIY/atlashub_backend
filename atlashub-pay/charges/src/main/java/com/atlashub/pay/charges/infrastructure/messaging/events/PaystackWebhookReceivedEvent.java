package com.atlashub.pay.charges.infrastructure.messaging.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaystackWebhookReceivedEvent(String eventType, String eventId, String environment,
                                           Map<String, Object> data, Payload payload) {
    public PaystackWebhookReceivedEvent {
        data = data == null ? Map.of() : Map.copyOf(data);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(String eventId, String environment, String eventType, Map<String, Object> data) {
        public Payload {
            data = data == null ? Map.of() : Map.copyOf(data);
        }
    }

    public String resolveEventType() {
        if (payload != null && payload.eventType() != null && !payload.eventType().isBlank()) {
            return payload.eventType();
        }
        return eventType;
    }

    public String resolveEnvironment() {
        if (payload != null && payload.environment() != null && !payload.environment().isBlank()) {
            return payload.environment();
        }
        return environment != null ? environment : "TEST";
    }

    public Map<String, Object> resolveData() {
        if (payload != null && payload.data() != null) {
            return payload.data();
        }
        return data != null ? data : Map.of();
    }
}
