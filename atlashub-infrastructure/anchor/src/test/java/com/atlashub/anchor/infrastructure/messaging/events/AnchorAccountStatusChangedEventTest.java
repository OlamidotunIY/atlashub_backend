package com.atlashub.anchor.infrastructure.messaging.events;

import com.atlashub.anchor.configuration.AnchorEnvironment;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnchorAccountStatusChangedEventTest {

    @Test
    void retains_the_provider_event_identity_used_for_consumer_deduplication() {
        AnchorAccountStatusChangedEvent event = new AnchorAccountStatusChangedEvent(
                "anchor-event-1", AnchorEnvironment.SANDBOX, ZonedDateTime.parse("2026-10-02T10:00:00Z"),
                "DEPOSIT_ACCOUNT", "anchor-account-1", "ACTIVE", "AtlasHub Ltd", "0123456789",
                "******6789", "Example Bank", "090000", null
        );

        assertEquals("anchor-event-1", event.eventId());
        assertEquals(AnchorEnvironment.SANDBOX, event.environment());
        assertEquals("DEPOSIT_ACCOUNT", event.resourceType());
    }

    @Test
    void rejects_a_missing_provider_event_identity() {
        assertThrows(IllegalArgumentException.class, () -> new AnchorAccountStatusChangedEvent(
                " ", AnchorEnvironment.LIVE, ZonedDateTime.now(), "DEPOSIT_ACCOUNT", "anchor-account-1",
                "ACTIVE", null, null, null, null, null, null
        ));
    }
}
