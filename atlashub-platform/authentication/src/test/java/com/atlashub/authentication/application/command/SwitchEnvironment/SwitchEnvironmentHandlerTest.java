package com.atlashub.authentication.application.command.SwitchEnvironment;

import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.exceptions.LiveEnvironmentUnavailableException;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.port.ComplianceQueryPort;
import com.atlashub.shared.application.port.MembershipQueryPort;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SwitchEnvironmentHandlerTest {

    @Test
    void refuses_live_mode_until_the_active_organization_is_compliance_approved() {
        SessionRepository sessions = mock(SessionRepository.class);
        MembershipQueryPort memberships = mock(MembershipQueryPort.class);
        ComplianceQueryPort compliance = mock(ComplianceQueryPort.class);
        TokenPort tokens = mock(TokenPort.class);
        Session current = Session.create(
                10L, "hash", "2", 3L, "TEST", "fingerprint", "family",
                ZonedDateTime.now().plusDays(30), null, null);
        when(sessions.findById(10L)).thenReturn(Optional.of(current));
        when(compliance.isApproved(3L)).thenReturn(false);

        SwitchEnvironmentHandler handler = new SwitchEnvironmentHandler(
                sessions, memberships, compliance, tokens);

        assertThrows(LiveEnvironmentUnavailableException.class,
                () -> handler.execute(new SwitchEnvironmentCommand(2L, 10L, "LIVE")));
    }
}
