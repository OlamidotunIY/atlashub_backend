package com.atlashub.anchor.infrastructure.messaging.events;

import com.atlashub.anchor.configuration.AnchorEnvironment;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A verified Anchor account lifecycle fact, normalized at the provider boundary.
 *
 * <p>Consumer modules may use this provider event directly. They remain responsible for resolving
 * it against their local aggregates and publishing their own AtlasHub business events.</p>
 */
public record AnchorAccountStatusChangedEvent(
        String eventId,
        AnchorEnvironment environment,
        ZonedDateTime occurredAt,
        String resourceType,
        String anchorResourceId,
        String status,
        String accountName,
        String accountNumber,
        String maskedAccountNumber,
        String bankName,
        String bankCode,
        String failureReason
) {
    public AnchorAccountStatusChangedEvent {
        requireText(eventId, "Anchor event ID");
        Objects.requireNonNull(environment, "Anchor environment is required");
        Objects.requireNonNull(occurredAt, "Event occurrence time is required");
        requireText(resourceType, "Anchor resource type");
        requireText(anchorResourceId, "Anchor resource ID");
        requireText(status, "Anchor account status");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
