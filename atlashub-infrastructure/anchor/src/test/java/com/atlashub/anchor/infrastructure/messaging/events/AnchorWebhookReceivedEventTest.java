package com.atlashub.anchor.infrastructure.messaging.events;

import com.atlashub.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.configuration.AnchorWebhookConsumer;
import com.atlashub.anchor.dto.common.AnchorResourceIdentifier;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnchorWebhookReceivedEventTest {

    @Test
    void snapshots_relationships_before_publishing_to_consumers() {
        Map<String, AnchorResourceIdentifier> relationships = new HashMap<>();
        relationships.put("account", new AnchorResourceIdentifier("anchor-account-1", "DepositAccount"));

        AnchorWebhookReceivedEvent event = new AnchorWebhookReceivedEvent(
                "anchor-event-1", AnchorEnvironment.LIVE, AnchorWebhookConsumer.PAY_ACCOUNTS,
                "account.opened", "2026-10-02T10:00:00", relationships
        );
        relationships.clear();

        assertEquals("anchor-account-1", event.relationships().get("account").id());
    }
}
