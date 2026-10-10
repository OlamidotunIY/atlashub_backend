package com.atlashub.anchor.infrastructure.external.anchor.dto.reservedaccount;

import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorBank;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRelationship;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservedAccountResource(
        String id,
        String type,
        Attributes attributes,
        Relationships relationships
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attributes(AnchorBank bank, String accountName, String accountNumber) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Relationships(AnchorRelationship merchant, AnchorRelationship customer, AnchorRelationship payoutAccount) {
    }
}
