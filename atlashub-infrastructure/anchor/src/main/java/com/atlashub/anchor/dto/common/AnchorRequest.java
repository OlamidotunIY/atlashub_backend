package com.atlashub.anchor.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** JSON:API-style request envelope used by Anchor endpoints. */
public record AnchorRequest<T>(@JsonProperty("data") T data) {
    public AnchorRequest {
        Objects.requireNonNull(data, "Anchor request data is required");
    }
}
