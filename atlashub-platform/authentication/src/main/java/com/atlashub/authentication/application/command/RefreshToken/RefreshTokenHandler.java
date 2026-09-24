package com.atlashub.authentication.application.command.RefreshToken;

import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.exceptions.AuthLocked;
import com.atlashub.authentication.domain.exceptions.InvalidTokenException;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class RefreshTokenHandler extends Command<RefreshTokenCommand, RefreshTokenResponse> {

    final SessionRepository sessionRepository;
    final AuthAccountRepository accountRepository;
    final TokenPort tokenPort;
    final MembershipQueryPort membershipQueryPort;
    final UserQueryPort userQueryPort;

    public RefreshTokenHandler(SessionRepository sessionRepository,
                               AuthAccountRepository accountRepository,
                               TokenPort tokenPort,
                               MembershipQueryPort membershipQueryPort,
                               UserQueryPort userQueryPort) {
        this.sessionRepository = sessionRepository;
        this.accountRepository = accountRepository;
        this.tokenPort = tokenPort;
        this.membershipQueryPort = membershipQueryPort;
        this.userQueryPort = userQueryPort;
    }

    @Override
    public RefreshTokenResponse execute(RefreshTokenCommand input) {
        Optional<Session> existingSession = sessionRepository.findByToken(input.refreshToken());

        if (existingSession.isEmpty()) {
            throw new InvalidTokenException();
        }

        Session current = existingSession.get();

        if (current.isExpired()) {
            sessionRepository.deleteByToken(input.refreshToken());
            throw new InvalidTokenException();
        }

        AuthAccount account = accountRepository.findByUserId(Long.parseLong(current.getUserId()))
                .orElseThrow(InvalidTokenException::new);

        if (account.isLocked()) {
            sessionRepository.deleteByToken(input.refreshToken());
            throw new AuthLocked();
        }

        Long orgId = userQueryPort.getActiveOrganizationId(account.getUserId())
                .orElseThrow(() -> new NotFoundException("Active organization not found"));
        Set<String> permissions = membershipQueryPort.getPermissions(account.getUserId(), orgId);
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime accessTokenExpiresAt = now.plusMinutes(30);
        ZonedDateTime newSessionExpiresAt = now.plusHours(24);
        String newRawToken = UUID.randomUUID().toString();
        Long newSessionId = sessionRepository.nextIdentity();

        sessionRepository.deleteByToken(input.refreshToken());

        TokenPort.AccessTokenResult accessToken = tokenPort.generateAccessToken(
                new TokenPort.AccessTokenPayload(account.getUserId().toString(), newSessionId.toString(), orgId.toString(), permissions));

        Session newSession = Session.create(
                newSessionId,
                newRawToken,
                account.getUserId().toString(),
                newSessionExpiresAt,
                current.getIpAddress(),
                current.getUserAgent()
        );
        sessionRepository.save(newSession);

        return new RefreshTokenResponse(accessToken.token(), accessTokenExpiresAt, newRawToken, newSessionExpiresAt);
    }
}
