package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.RefreshTokenCommand;
import com.atlashub.auth.application.dto.AuthTokenDto;
import com.atlashub.auth.application.service.TokenIssuanceService;
import com.atlashub.auth.domain.exception.AuthErrorCode;
import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.auth.domain.model.Session;
import com.atlashub.auth.domain.model.SessionStatus;
import com.atlashub.auth.domain.repository.AuthAccountRepository;
import com.atlashub.auth.domain.repository.SessionRepository;
import com.atlashub.shared.dto.ApiResponse;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.BusinessRuleException;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class RefreshTokenUseCase extends BaseUseCase<RefreshTokenCommand, ApiResponse<AuthTokenDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final SessionRepository sessionRepository;
    private final TokenIssuanceService tokenIssuanceService;
    private final DomainEventPublisher eventPublisher;

    public RefreshTokenUseCase(
            AuthAccountRepository authAccountRepository,
            SessionRepository sessionRepository,
            TokenIssuanceService tokenIssuanceService,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.sessionRepository = sessionRepository;
        this.tokenIssuanceService = tokenIssuanceService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<AuthTokenDto> execute(RefreshTokenCommand input) {
        Session session = sessionRepository.findByToken(input.refreshToken())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.SESSION_NOT_FOUND, "Session not found"));

        if (session.getStatus() == SessionStatus.REVOKED) {
            throw new BusinessRuleException(AuthErrorCode.SESSION_ALREADY_REVOKED, "Session is revoked");
        }
        if (session.getStatus() == SessionStatus.EXPIRED || ZonedDateTime.now().isAfter(session.getExpiresAt())) {
            throw new BusinessRuleException(AuthErrorCode.REFRESH_TOKEN_EXPIRED, "Refresh token expired");
        }

        AuthAccount authAccount = authAccountRepository.findById(session.getAuthAccountId())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        session.expire();
        sessionRepository.save(session);
        publishEvents(session, eventPublisher);

        AuthTokenDto tokenDto = tokenIssuanceService.issueTokensAndCreateSession(authAccount, input.ipAddress(), input.userAgent());
        return new ApiResponse<>(true, "Token refresh successful", tokenDto, null);
    }
}
