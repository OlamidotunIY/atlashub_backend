package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.query.GetUserInfo.GetUserInfoHandler;
import com.atlashub.authentication.application.query.GetUserInfo.GetUserInfoResult;
import com.atlashub.authentication.presentation.dto.UserInfoResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserInfoControllerTest {

    private GetUserInfoHandler handler;
    private UserInfoController controller;

    @BeforeEach
    void setUp() {
        handler = mock(GetUserInfoHandler.class);
        controller = new UserInfoController(handler);
    }

    @Test
    void returns_user_info_for_authenticated_principal() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                42L, 100L, "LIVE", "session_1", "jti_1", ZonedDateTime.now().plusHours(1)
        );
        GetUserInfoResult result = new GetUserInfoResult(
                "42", "jane@example.com", true, "Jane Doe", "Jane", "Doe", 100L, "LIVE"
        );
        when(handler.execute(any())).thenReturn(result);

        ResponseEntity<ApiResponse<UserInfoResponse>> response = controller.getUserInfo(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("42", response.getBody().data().sub());
        assertEquals("jane@example.com", response.getBody().data().email());
        assertEquals("Jane Doe", response.getBody().data().name());
    }

    @Test
    void returns_unauthorized_when_principal_is_null() {
        ResponseEntity<ApiResponse<UserInfoResponse>> response = controller.getUserInfo(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
