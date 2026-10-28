package com.atlashub.authentication.application.query.GetOpenIdConfiguration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetOpenIdConfigurationHandlerTest {

    @Test
    void returns_rfc_8414_compliant_metadata() {
        GetOpenIdConfigurationHandler handler = new GetOpenIdConfigurationHandler("https://api.atlashub.com");

        GetOpenIdConfigurationResult result = handler.execute(new GetOpenIdConfigurationQuery("https://api.atlashub.com"));

        assertEquals("https://api.atlashub.com", result.issuer());
        assertEquals("https://api.atlashub.com/api/v1/oauth2/authorize", result.authorizationEndpoint());
        assertEquals("https://api.atlashub.com/api/v1/oauth2/token", result.tokenEndpoint());
        assertEquals("https://api.atlashub.com/api/v1/oauth2/userinfo", result.userinfoEndpoint());
        assertEquals("https://api.atlashub.com/.well-known/jwks.json", result.jwksUri());
        assertTrue(result.responseTypesSupported().contains("code"));
        assertTrue(result.codeChallengeMethodsSupported().contains("S256"));
        assertTrue(result.idTokenSigningAlgValuesSupported().contains("RS256"));
        assertTrue(result.scopesSupported().contains("openid"));
        assertTrue(result.grantTypesSupported().contains("authorization_code"));
    }
}
