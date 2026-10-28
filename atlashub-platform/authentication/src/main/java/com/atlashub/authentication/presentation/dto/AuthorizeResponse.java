package com.atlashub.authentication.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorizeResponse(
        String code,
        String state,
        @JsonProperty("redirect_uri")
        String redirectUri
) {
}
