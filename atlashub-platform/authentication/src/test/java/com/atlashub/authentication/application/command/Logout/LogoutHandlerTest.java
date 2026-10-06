package com.atlashub.authentication.application.command.Logout;

import com.atlashub.authentication.application.port.TokenRevocationPort;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogoutHandlerTest {

    @Test
    void invalidates_only_the_principal_session_and_revokes_its_jti() {
        SessionRepository sessions = mock(SessionRepository.class);
        TokenRevocationPort revocations = mock(TokenRevocationPort.class);
        Session session = Session.create(
                10L, "hash", "2", 3L, "TEST", "fingerprint", "family",
                ZonedDateTime.now().plusDays(30), null, null);
        when(sessions.findById(10L)).thenReturn(Optional.of(session));

        new LogoutHandler(sessions, revocations).execute(
                new LogoutCommand(2L, 10L, "jti", ZonedDateTime.now().plusMinutes(10)));

        verify(sessions).deleteById(10L);
        verify(revocations).revokeAccessToken(eq("jti"), any());
    }
}
