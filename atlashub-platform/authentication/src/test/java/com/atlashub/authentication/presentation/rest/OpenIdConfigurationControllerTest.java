package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.query.GetOpenIdConfiguration.GetOpenIdConfigurationHandler;
import com.atlashub.authentication.application.query.GetOpenIdConfiguration.GetOpenIdConfigurationResult;
import com.atlashub.authentication.presentation.dto.OpenIdConfigurationResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenIdConfigurationControllerTest {

    private GetOpenIdConfigurationHandler handler;
    private OpenIdConfigurationController controller;
    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        handler = mock(GetOpenIdConfigurationHandler.class);
        controller = new OpenIdConfigurationController(handler);
        httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getScheme()).thenReturn("https");
        when(httpRequest.getServerName()).thenReturn("api.atlashub.com");
        when(httpRequest.getServerPort()).thenReturn(443);
        when(httpRequest.getContextPath()).thenReturn("");
    }

    @Test
    void returns_openid_configuration_metadata() {
        GetOpenIdConfigurationResult result = new GetOpenIdConfigurationResult(
                "https://api.atlashub.com",
                "https://api.atlashub.com/api/v1/oauth2/authorize",
                "https://api.atlashub.com/api/v1/oauth2/token",
                "https://api.atlashub.com/api/v1/oauth2/userinfo",
                "https://api.atlashub.com/.well-known/jwks.json",
                List.of("code"), List.of("public"), List.of("RS256"), List.of("S256"),
                List.of("openid", "profile"), List.of("none"), List.of("authorization_code")
        );
        when(handler.execute(any())).thenReturn(result);

        ResponseEntity<ApiResponse<OpenIdConfigurationResponse>> response = controller.getConfiguration(httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("https://api.atlashub.com", response.getBody().data().issuer());
        assertEquals("https://api.atlashub.com/api/v1/oauth2/authorize", response.getBody().data().authorizationEndpoint());
    }
}
