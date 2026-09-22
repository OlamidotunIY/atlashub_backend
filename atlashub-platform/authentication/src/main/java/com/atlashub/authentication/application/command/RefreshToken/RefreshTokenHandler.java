package com.atlashub.authentication.application.command.RefreshToken;

import com.atlashub.authentication.application.port.SessionPort;
import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.exceptions.AuthLocked;
import com.atlashub.authentication.domain.exceptions.InvalidTokenException;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.valueobject.Session;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.service.HashingUtils;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class RefreshTokenHandler extends Command<RefreshTokenCommand, RefreshTokenResponse> {

    final SessionPort sessionPort;
    final AuthAccountRepository accountRepository;
    final TokenPort tokenPort;
    final MembershipQueryPort membershipQueryPort;

    public RefreshTokenHandler(SessionPort sessionPort, AuthAccountRepository accountRepository,
                               TokenPort tokenPort, MembershipQueryPort membershipQueryPort) {
        this.sessionPort = sessionPort;
        this.accountRepository = accountRepository;
        this.tokenPort = tokenPort;
        this.membershipQueryPort = membershipQueryPort;
    }

    @Override
    public RefreshTokenResponse execute(RefreshTokenCommand input) {
        String hash = HashingUtils.sha256Hex(input.refreshToken());
        Optional<Session> existingSession = sessionPort.findByTokenHash(hash);

        if (existingSession.isEmpty()) {
            throw new InvalidTokenException();
        }

        if (ZonedDateTime.now().isAfter(existingSession.get().refreshTokenExpiresAt())) {
            sessionPort.delete(hash);
            throw new InvalidTokenException();
        }

        AuthAccount account = accountRepository.findById(existingSession.get().authAccountId()).orElseThrow(InvalidTokenException::new);

        if (account.isLocked()) {
            sessionPort.delete(hash);
            throw new AuthLocked();
        }

        Long orgId = existingSession.get().orgId();
        String newRawRefreshToken = UUID.randomUUID().toString();
        String newHash = HashingUtils.sha256Hex(newRawRefreshToken);
        ZonedDateTime accessTokenExpiresAt = ZonedDateTime.now().plusMinutes(30);
        ZonedDateTime refreshTokenExpiresAt = ZonedDateTime.now().plusHours(24);
        Set<String> permissions = membershipQueryPort.getPermissions(account.getUserId(), orgId);

        sessionPort.delete(hash);

        TokenPort.AccessTokenResult accessToken = tokenPort.generateAccessToken(
                new TokenPort.AccessTokenPayload(account.getUserId().toString(), orgId.toString(), permissions));

        Session session = new Session(account.getId(), newHash, accessToken.jti(),
                accessTokenExpiresAt, refreshTokenExpiresAt, existingSession.get().deviceId(), orgId);
        sessionPort.save(session);

        return new RefreshTokenResponse(accessToken.token(), accessTokenExpiresAt, newRawRefreshToken, refreshTokenExpiresAt);
    }
}
