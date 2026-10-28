package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.command.ExchangeToken.ExchangeTokenHandler;
import com.atlashub.authentication.application.command.ExchangeToken.ExchangeTokenResult;
import com.atlashub.authentication.presentation.dto.TokenRequest;
import com.atlashub.authentication.presentation.dto.TokenResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2TokenControllerTest {

    private ExchangeTokenHandler handler;
    private OAuth2TokenController controller;
    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        handler = mock(ExchangeTokenHandler.class);
        controller = new OAuth2TokenController(handler);
        httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("TestClient");
    }

    @Test
    void exchanges_token_via_form_post() {
        when(handler.execute(any())).thenReturn(
                new ExchangeTokenResult("access_token_123", "Bearer", 900L, "refresh_token_123", "id_token_123", "openid")
        );

        ResponseEntity<ApiResponse<TokenResponse>> response = controller.tokenForm(
                "authorization_code", "code_xyz", "http://localhost:3000/callback",
                "atlashub-web", null, "verifier_abc", null, "fp_1", httpRequest
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access_token_123", response.getBody().data().accessToken());
        assertEquals("Bearer", response.getBody().data().tokenType());
        assertEquals(900L, response.getBody().data().expiresIn());
        assertEquals("refresh_token_123", response.getBody().data().refreshToken());
        assertEquals("id_token_123", response.getBody().data().idToken());
    }

    @Test
    void exchanges_token_via_json_post() {
        when(handler.execute(any())).thenReturn(
                new ExchangeTokenResult("access_token_456", "Bearer", 900L, "refresh_token_456", null, null)
        );

        TokenRequest request = new TokenRequest(
                "refresh_token", null, null, "atlashub-web", null, null, "refresh_token_old"
        );

        ResponseEntity<ApiResponse<TokenResponse>> response = controller.tokenJson(request, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access_token_456", response.getBody().data().accessToken());
    }
}
