package com.atlashub.anchor.dto.webhook;

import com.atlashub.anchor.dto.common.AnchorRelationship;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/** The minimal JSON:API event envelope delivered by Anchor to a webhook endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnchorWebhookPayload(EventData data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventData(
            String id,
            String type,
            Attributes attributes,
            Map<String, AnchorRelationship> relationships
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attributes(String createdAt) {
    }
}
