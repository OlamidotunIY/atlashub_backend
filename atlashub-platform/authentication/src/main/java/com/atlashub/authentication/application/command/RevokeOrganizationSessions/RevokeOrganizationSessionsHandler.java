package com.atlashub.authentication.application.command.RevokeOrganizationSessions;

import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class RevokeOrganizationSessionsHandler extends Command<RevokeOrganizationSessionsCommand, Void> {
    private final SessionRepository sessionRepository;

    public RevokeOrganizationSessionsHandler(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Void execute(RevokeOrganizationSessionsCommand command) {
        sessionRepository.deleteAllByOrganizationId(command.organizationId());
        return null;
    }
}
