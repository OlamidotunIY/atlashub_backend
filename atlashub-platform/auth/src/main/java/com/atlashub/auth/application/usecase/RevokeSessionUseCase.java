package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.RevokeSessionCommand;
import com.atlashub.auth.domain.exception.AuthErrorCode;
import com.atlashub.auth.domain.model.Session;
import com.atlashub.auth.domain.repository.SessionRepository;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevokeSessionUseCase extends BaseUseCase<RevokeSessionCommand, ApiResponse<Void>> {

    private final SessionRepository sessionRepository;
    private final DomainEventPublisher eventPublisher;

    public RevokeSessionUseCase(SessionRepository sessionRepository, DomainEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<Void> execute(RevokeSessionCommand input) {
        Session session = sessionRepository.findByToken(input.token())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.SESSION_NOT_FOUND, "Session not found"));

        session.revoke();
        sessionRepository.save(session);
        publishEvents(session, eventPublisher);

        return new ApiResponse<>(true, "Session revoked successfully", null, null);
    }
}
