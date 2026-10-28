package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.command.IssueAuthorizationCode.IssueAuthorizationCodeHandler;
import com.atlashub.authentication.application.command.IssueAuthorizationCode.IssueAuthorizationCodeResult;
import com.atlashub.authentication.presentation.dto.AuthorizeRequest;
import com.atlashub.authentication.presentation.dto.AuthorizeResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2AuthorizeControllerTest {

    private IssueAuthorizationCodeHandler handler;
    private OAuth2AuthorizeController controller;

    @BeforeEach
    void setUp() {
        handler = mock(IssueAuthorizationCodeHandler.class);
        controller = new OAuth2AuthorizeController(handler);
    }

    @Test
    void get_authorize_redirects_with_authorization_code() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                42L, 100L, "LIVE", "session_1", "jti_1", ZonedDateTime.now().plusHours(1)
        );
        when(handler.execute(any())).thenReturn(
                new IssueAuthorizationCodeResult("auth_code_123", "http://localhost:3000/callback", "state_xyz")
        );

        ResponseEntity<ApiResponse<AuthorizeResponse>> response = controller.authorizeGet(
                "code", "atlashub-web", "http://localhost:3000/callback", "openid", "state_xyz",
                "challenge", "S256", null, principal
        );

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());
        assertTrue(response.getHeaders().getLocation().toString().contains("code=auth_code_123"));
        assertTrue(response.getHeaders().getLocation().toString().contains("state=state_xyz"));
    }

    @Test
    void post_authorize_returns_ok_with_code_payload() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                42L, 100L, "LIVE", "session_1", "jti_1", ZonedDateTime.now().plusHours(1)
        );
        when(handler.execute(any())).thenReturn(
                new IssueAuthorizationCodeResult("auth_code_123", "http://localhost:3000/callback", "state_xyz")
        );

        AuthorizeRequest request = new AuthorizeRequest(
                "code", "atlashub-web", "http://localhost:3000/callback",
                "openid", "state_xyz", "challenge", "S256", null
        );

        ResponseEntity<ApiResponse<AuthorizeResponse>> response = controller.authorizePost(request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("auth_code_123", response.getBody().data().code());
    }

    @Test
    void returns_unauthorized_when_principal_is_null() {
        AuthorizeRequest request = new AuthorizeRequest(
                "code", "atlashub-web", "http://localhost:3000/callback",
                "openid", "state_xyz", "challenge", "S256", null
        );

        ResponseEntity<ApiResponse<AuthorizeResponse>> response = controller.authorizePost(request, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
