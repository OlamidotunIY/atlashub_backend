package com.atlashub.anchor.dto.webhook;

import com.atlashub.anchor.dto.common.AnchorRelationship;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.List;

/** The minimal JSON:API event envelope delivered by Anchor to a webhook endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnchorWebhookPayload(EventData data, List<IncludedData> included) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventData(
            String id,
            String type,
            Map<String, Object> attributes,
            Map<String, AnchorRelationship> relationships
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IncludedData(
            String id,
            String type,
            Map<String, Object> attributes,
            Map<String, AnchorRelationship> relationships
    ) {
    }
}
