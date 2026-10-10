package com.atlashub.compliance.infrastructure.messaging.events;

import java.util.List;
import java.util.Map;

/** Local wire contract for verified Anchor events consumed by compliance. */
public record AnchorWebhookReceivedEvent(
        String eventId,
        String environment,
        String consumer,
        String eventType,
        String occurredAt,
        Map<String, Object> attributes,
        Map<String, ResourceIdentifier> relationships,
        List<IncludedResource> includedResources
) {
    public AnchorWebhookReceivedEvent {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        relationships = relationships == null ? Map.of() : Map.copyOf(relationships);
        includedResources = includedResources == null ? List.of() : List.copyOf(includedResources);
    }

    public record ResourceIdentifier(String id, String type) {
    }

    public record IncludedResource(
            String id,
            String type,
            Map<String, Object> attributes,
            Map<String, ResourceIdentifier> relationships
    ) {
        public IncludedResource {
            attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
            relationships = relationships == null ? Map.of() : Map.copyOf(relationships);
        }
    }
}
