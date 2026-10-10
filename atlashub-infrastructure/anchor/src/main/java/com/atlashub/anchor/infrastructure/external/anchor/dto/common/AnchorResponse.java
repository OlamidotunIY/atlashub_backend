package com.atlashub.anchor.infrastructure.external.anchor.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** JSON:API-style response envelope used by Anchor endpoints. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnchorResponse<T>(@JsonProperty("data") T data) {
}
