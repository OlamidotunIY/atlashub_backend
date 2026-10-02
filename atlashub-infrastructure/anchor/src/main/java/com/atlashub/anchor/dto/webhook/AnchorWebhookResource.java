package com.atlashub.anchor.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Anchor's persisted webhook-subscription resource. The secret token is deliberately omitted. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnchorWebhookResource(String id, String type, Attributes attributes) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attributes(
            String createdAt,
            Boolean allEventsEnabled,
            String deliveryMode,
            List<String> enabledEvents,
            String label,
            String url,
            String status,
            Boolean supportIncluded
    ) {
    }
}
