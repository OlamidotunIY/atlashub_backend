package com.atlashub.anchor.infrastructure.external.anchor.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnchorRelationship(AnchorResourceIdentifier data) {
}
