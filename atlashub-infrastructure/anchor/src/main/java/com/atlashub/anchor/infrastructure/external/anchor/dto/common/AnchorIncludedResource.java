package com.atlashub.anchor.infrastructure.external.anchor.dto.common;

import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

/** Immutable provider resource included with an Anchor webhook delivery. */
public record AnchorIncludedResource(
        String id,
        String type,
        Map<String, Object> attributes,
        Map<String, AnchorResourceIdentifier> relationships
) {
    public AnchorIncludedResource {
        attributes = attributes == null ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        relationships = relationships == null ? Map.of() : Map.copyOf(relationships);
    }
}
