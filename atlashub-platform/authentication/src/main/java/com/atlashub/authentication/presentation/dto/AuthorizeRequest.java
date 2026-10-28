package com.atlashub.authentication.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AuthorizeRequest(
        @NotBlank(message = "response_type is required")
        @JsonProperty("response_type")
        String responseType,

        @NotBlank(message = "client_id is required")
        @JsonProperty("client_id")
        String clientId,

        @NotBlank(message = "redirect_uri is required")
        @JsonProperty("redirect_uri")
        String redirectUri,

        String scope,

        String state,

        @NotBlank(message = "code_challenge is required for PKCE")
        @JsonProperty("code_challenge")
        String codeChallenge,

        @NotBlank(message = "code_challenge_method is required")
        @JsonProperty("code_challenge_method")
        String codeChallengeMethod,

        String nonce
) {
}
