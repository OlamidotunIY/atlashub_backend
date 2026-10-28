package com.atlashub.authentication.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenIdConfigurationResponse(
        String issuer,

        @JsonProperty("authorization_endpoint")
        String authorizationEndpoint,

        @JsonProperty("token_endpoint")
        String tokenEndpoint,

        @JsonProperty("userinfo_endpoint")
        String userinfoEndpoint,

        @JsonProperty("jwks_uri")
        String jwksUri,

        @JsonProperty("response_types_supported")
        List<String> responseTypesSupported,

        @JsonProperty("subject_types_supported")
        List<String> subjectTypesSupported,

        @JsonProperty("id_token_signing_alg_values_supported")
        List<String> idTokenSigningAlgValuesSupported,

        @JsonProperty("code_challenge_methods_supported")
        List<String> codeChallengeMethodsSupported,

        @JsonProperty("scopes_supported")
        List<String> scopesSupported,

        @JsonProperty("token_endpoint_auth_methods_supported")
        List<String> tokenEndpointAuthMethodsSupported,

        @JsonProperty("grant_types_supported")
        List<String> grantTypesSupported
) {
}
