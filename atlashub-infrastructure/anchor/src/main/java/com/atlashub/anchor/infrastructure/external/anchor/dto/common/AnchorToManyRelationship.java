package com.atlashub.anchor.infrastructure.external.anchor.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnchorToManyRelationship(List<AnchorResourceIdentifier> data) {
}
