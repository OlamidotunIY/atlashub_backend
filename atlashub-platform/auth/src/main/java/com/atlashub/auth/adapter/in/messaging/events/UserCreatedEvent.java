package com.atlashub.auth.adapter.in.messaging.events;

// Local mirror of identity module's UserCreated domain event.
// If UserCreated.Payload fields change in the identity module, update this record to match.
public record UserCreatedEvent(
    String eventId,
    String aggregateId,
    Payload payload
) {
    public record Payload(
        String email,
        String firstName,
        String lastName,
        String country
    ) {}
}
