package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.RevokeAllSessionsCommand;
import com.atlashub.auth.domain.model.Session;
import com.atlashub.auth.domain.valueobject.SessionStatus;
import com.atlashub.auth.domain.repository.SessionRepository;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RevokeAllSessionsUseCase extends BaseUseCase<RevokeAllSessionsCommand, ApiResponse<Void>> {

    private final SessionRepository sessionRepository;
    private final DomainEventPublisher eventPublisher;

    public RevokeAllSessionsUseCase(SessionRepository sessionRepository, DomainEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<Void> execute(RevokeAllSessionsCommand input) {
        List<Session> activeSessions = sessionRepository.findByAuthAccountIdAndStatus(input.authAccountId(), SessionStatus.ACTIVE);
        
        for (Session session : activeSessions) {
            session.revoke();
            publishEvents(session, eventPublisher);
        }
        
        sessionRepository.saveAll(activeSessions);
        
        return new ApiResponse<>(true, "All active sessions revoked successfully", null, null);
    }
}

