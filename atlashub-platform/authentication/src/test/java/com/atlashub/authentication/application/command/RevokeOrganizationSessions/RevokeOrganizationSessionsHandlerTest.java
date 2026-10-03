package com.atlashub.authentication.application.command.RevokeOrganizationSessions;

import com.atlashub.authentication.domain.repositories.SessionRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RevokeOrganizationSessionsHandlerTest {

    @Test
    void invalidates_every_runtime_session_for_the_organization() {
        SessionRepository sessions = mock(SessionRepository.class);

        new RevokeOrganizationSessionsHandler(sessions)
                .execute(new RevokeOrganizationSessionsCommand(20L));

        verify(sessions).deleteAllByOrganizationId(20L);
    }
}
