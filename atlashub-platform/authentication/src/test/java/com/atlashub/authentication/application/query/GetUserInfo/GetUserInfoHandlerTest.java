package com.atlashub.authentication.application.query.GetUserInfo;

import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserInfoHandlerTest {

    private UserQueryPort userQueryPort;
    private GetUserInfoHandler handler;

    @BeforeEach
    void setUp() {
        userQueryPort = mock(UserQueryPort.class);
        handler = new GetUserInfoHandler(userQueryPort);
    }

    @Test
    void returns_user_claims_for_existing_user() {
        UserQueryPort.UserDto user = new UserQueryPort.UserDto(
                42L, "Jane", "Doe", "jane@example.com", "NG", 100L, ApiEnvironment.LIVE, true
        );
        when(userQueryPort.findById(42L)).thenReturn(Optional.of(user));

        GetUserInfoResult result = handler.execute(new GetUserInfoQuery(42L));

        assertEquals("42", result.sub());
        assertEquals("jane@example.com", result.email());
        assertTrue(result.emailVerified());
        assertEquals("Jane Doe", result.name());
        assertEquals("Jane", result.givenName());
        assertEquals("Doe", result.familyName());
        assertEquals(100L, result.activeOrganizationId());
        assertEquals("LIVE", result.environment());
    }

    @Test
    void throws_not_found_when_user_does_not_exist() {
        when(userQueryPort.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> handler.execute(new GetUserInfoQuery(999L)));
    }
}
