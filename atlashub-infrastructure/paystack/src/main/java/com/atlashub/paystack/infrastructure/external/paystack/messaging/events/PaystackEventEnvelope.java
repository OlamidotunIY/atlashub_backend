package com.atlashub.paystack.infrastructure.external.paystack.messaging.events;

public record PaystackEventEnvelope(String eventType, String eventId, Object payload) {}
