package com.atlashub.authentication.application.command.RevokeUserSessions;

import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class RevokeUserSessionsHandler extends Command<RevokeUserSessionsCommand, Void> {
    private final SessionRepository sessionRepository;

    public RevokeUserSessionsHandler(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Void execute(RevokeUserSessionsCommand command) {
        sessionRepository.deleteAllByUserId(command.userId().toString());
        return null;
    }
}
