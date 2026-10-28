package com.atlashub.authentication.application.command.SwitchEnvironment;

import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SwitchEnvironmentHandlerTest {

    @Test
    void permits_live_mode_without_an_organization() {
        SessionRepository sessions = mock(SessionRepository.class);
        MembershipQueryPort memberships = mock(MembershipQueryPort.class);
        TokenPort tokens = mock(TokenPort.class);
        DomainEventPublisher events = mock(DomainEventPublisher.class);
        Session current = Session.create(
                10L, "hash", "2", null, "TEST", "fingerprint", "family",
                ZonedDateTime.now().plusDays(30), null, null);
        when(sessions.findById(10L)).thenReturn(Optional.of(current));
        when(sessions.nextIdentity()).thenReturn(11L);
        when(tokens.generateAccessToken(any())).thenReturn(new TokenPort.AccessTokenResult(
                "access", "jti", ZonedDateTime.now().plusMinutes(15)));

        SwitchEnvironmentHandler handler = new SwitchEnvironmentHandler(
                sessions, memberships, tokens, events);

        var result = handler.execute(new SwitchEnvironmentCommand(2L, 10L, ApiEnvironment.LIVE));
        assertEquals("access", result.accessToken());
        verifyNoInteractions(memberships);
    }
}
