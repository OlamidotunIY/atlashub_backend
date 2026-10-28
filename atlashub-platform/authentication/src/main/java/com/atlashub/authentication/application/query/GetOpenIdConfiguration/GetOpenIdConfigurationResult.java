package com.atlashub.authentication.application.query.GetOpenIdConfiguration;

import java.util.List;

public record GetOpenIdConfigurationResult(
        String issuer,
        String authorizationEndpoint,
        String tokenEndpoint,
        String userinfoEndpoint,
        String jwksUri,
        List<String> responseTypesSupported,
        List<String> subjectTypesSupported,
        List<String> idTokenSigningAlgValuesSupported,
        List<String> codeChallengeMethodsSupported,
        List<String> scopesSupported,
        List<String> tokenEndpointAuthMethodsSupported,
        List<String> grantTypesSupported
) {
}
