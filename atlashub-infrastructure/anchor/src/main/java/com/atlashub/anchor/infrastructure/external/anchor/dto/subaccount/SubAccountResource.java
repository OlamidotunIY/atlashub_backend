package com.atlashub.anchor.infrastructure.external.anchor.dto.subaccount;

import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRelationship;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorToManyRelationship;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubAccountResource(
        String id,
        String type,
        Attributes attributes,
        Relationships relationships
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attributes(String accountType, String createdAt, List<String> metadata) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Relationships(
            AnchorRelationship customer,
            AnchorRelationship parentAccount,
            AnchorToManyRelationship virtualNubans
    ) {
    }
}
