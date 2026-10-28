package com.atlashub.authentication.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "grant_type is required")
        @JsonProperty("grant_type")
        String grantType,

        String code,

        @JsonProperty("redirect_uri")
        String redirectUri,

        @NotBlank(message = "client_id is required")
        @JsonProperty("client_id")
        String clientId,

        @JsonProperty("client_secret")
        String clientSecret,

        @JsonProperty("code_verifier")
        String codeVerifier,

        @JsonProperty("refresh_token")
        String refreshToken
) {
}
