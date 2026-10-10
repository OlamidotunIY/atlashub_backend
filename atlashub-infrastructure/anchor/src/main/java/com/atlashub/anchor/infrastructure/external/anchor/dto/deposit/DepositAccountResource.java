package com.atlashub.anchor.infrastructure.external.anchor.dto.deposit;

import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorBank;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRelationship;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DepositAccountResource(
        String id,
        String type,
        Attributes attributes,
        Relationships relationships
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attributes(
            String createdAt,
            AnchorBank bank,
            String accountName,
            Boolean frozen,
            String currency,
            String accountNumber,
            String type,
            String status
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Relationships(AnchorRelationship customer) {
    }
}
