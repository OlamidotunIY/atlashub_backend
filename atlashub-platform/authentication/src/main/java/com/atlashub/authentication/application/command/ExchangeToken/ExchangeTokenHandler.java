package com.atlashub.authentication.application.command.ExchangeToken;

import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenCommand;
import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenHandler;
import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenResponse;
import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.AuthorizationCode;
import com.atlashub.authentication.domain.entities.OAuthClient;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.exceptions.AuthLocked;
import com.atlashub.authentication.domain.exceptions.EmailVerificationRequired;
import com.atlashub.authentication.domain.exceptions.InvalidClientException;
import com.atlashub.authentication.domain.exceptions.InvalidCredentials;
import com.atlashub.authentication.domain.exceptions.InvalidGrantException;
import com.atlashub.authentication.domain.exceptions.OAuthException;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.AuthorizationCodeRepository;
import com.atlashub.authentication.domain.repositories.OAuthClientRepository;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.authentication.domain.services.PkceVerifier;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.service.HashingUtils;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class ExchangeTokenHandler extends Command<ExchangeTokenCommand, ExchangeTokenResult> {

    private final OAuthClientRepository oauthClientRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final SessionRepository sessionRepository;
    private final AuthAccountRepository authAccountRepository;
    private final UserQueryPort userQueryPort;
    private final MembershipQueryPort membershipQueryPort;
    private final TokenPort tokenPort;
    private final PkceVerifier pkceVerifier;
    private final PasswordEncoderPort passwordEncoderPort;
    private final RefreshTokenHandler refreshTokenHandler;

    public ExchangeTokenHandler(OAuthClientRepository oauthClientRepository,
                                AuthorizationCodeRepository authorizationCodeRepository,
                                SessionRepository sessionRepository,
                                AuthAccountRepository authAccountRepository,
                                UserQueryPort userQueryPort,
                                MembershipQueryPort membershipQueryPort,
                                TokenPort tokenPort,
                                PkceVerifier pkceVerifier,
                                PasswordEncoderPort passwordEncoderPort,
                                RefreshTokenHandler refreshTokenHandler) {
        this.oauthClientRepository = oauthClientRepository;
        this.authorizationCodeRepository = authorizationCodeRepository;
        this.sessionRepository = sessionRepository;
        this.authAccountRepository = authAccountRepository;
        this.userQueryPort = userQueryPort;
        this.membershipQueryPort = membershipQueryPort;
        this.tokenPort = tokenPort;
        this.pkceVerifier = pkceVerifier;
        this.passwordEncoderPort = passwordEncoderPort;
        this.refreshTokenHandler = refreshTokenHandler;
    }

    @Override
    @Transactional
    public ExchangeTokenResult execute(ExchangeTokenCommand command) {
        if (command.grantType() == null || command.grantType().isBlank()) {
            throw new OAuthException("invalid_request", "grant_type is required");
        }

        String grantType = command.grantType().trim();

        if (command.clientId() == null || command.clientId().isBlank()) {
            throw new InvalidClientException("client_id is required");
        }

        OAuthClient client = oauthClientRepository.findByClientId(command.clientId().trim())
                .orElseThrow(() -> new InvalidClientException("Client not found: " + command.clientId()));

        if (!client.isEnabled()) {
            throw new InvalidClientException("Client is disabled: " + command.clientId());
        }

        client.validateSecret(command.clientSecret(), passwordEncoderPort);

        if ("authorization_code".equals(grantType)) {
            return exchangeAuthorizationCode(command, client);
        } else if ("refresh_token".equals(grantType)) {
            return exchangeRefreshToken(command, client);
        } else {
            throw new OAuthException("unsupported_grant_type", "Grant type not supported: " + grantType);
        }
    }

    private ExchangeTokenResult exchangeAuthorizationCode(ExchangeTokenCommand command, OAuthClient client) {
        if (command.code() == null || command.code().isBlank()) {
            throw new InvalidGrantException("code is required for authorization_code grant");
        }

        String codeHash = HashingUtils.sha256Hex(command.code().trim());
        AuthorizationCode authCode = authorizationCodeRepository.findByCodeHash(codeHash)
                .orElseThrow(() -> new InvalidGrantException("Authorization code not found or invalid"));

        if (authCode.isUsed()) {
            throw new InvalidGrantException("Authorization code has already been used");
        }

        if (authCode.isExpired()) {
            throw new InvalidGrantException("Authorization code has expired");
        }

        authCode.markAsUsed();
        authorizationCodeRepository.save(authCode);

        authCode.validateClientId(client.getClientId());
        authCode.validateRedirectUri(command.redirectUri());

        pkceVerifier.verify(command.codeVerifier(), authCode.getCodeChallenge(), authCode.getCodeChallengeMethod());

        UserQueryPort.UserDto user = userQueryPort.findById(authCode.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + authCode.getUserId()));

        AuthAccount account = authAccountRepository.findByUserId(user.id())
                .orElseThrow(InvalidCredentials::new);

        if (account.isLocked()) {
            throw new AuthLocked();
        }

        if (!user.emailVerified()) {
            throw new EmailVerificationRequired();
        }

        String environment = user.activeEnvironment() != null ? user.activeEnvironment().name() : ApiEnvironment.TEST.name();
        Long orgId = user.activeOrganizationId();
        if (orgId != null && membershipQueryPort.getMemberStatus(user.id(), orgId)
                != MembershipQueryPort.MembershipStatus.ACTIVE) {
            orgId = null;
        }

        Set<String> permissions = orgId == null ? Set.of() : membershipQueryPort.getPermissions(user.id(), orgId);

        String rawRefreshToken = UUID.randomUUID().toString();
        Long sessionId = sessionRepository.nextIdentity();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime sessionExpiresAt = now.plusDays(30);

        String deviceFingerprint = (command.deviceFingerprint() != null && !command.deviceFingerprint().isBlank())
                ? command.deviceFingerprint().trim()
                : HashingUtils.sha256Hex(command.userAgent() == null ? "oauth-client" : command.userAgent());

        Session session = Session.create(
                sessionId,
                HashingUtils.sha256Hex(rawRefreshToken),
                user.id().toString(),
                orgId,
                environment,
                deviceFingerprint,
                UUID.randomUUID().toString(),
                sessionExpiresAt,
                command.ipAddress(),
                command.userAgent()
        );
        sessionRepository.save(session);

        TokenPort.AccessTokenResult accessTokenResult = tokenPort.generateAccessToken(
                new TokenPort.AccessTokenPayload(
                        user.id().toString(),
                        sessionId.toString(),
                        orgId == null ? null : orgId.toString(),
                        environment,
                        permissions
                )
        );

        String idTokenString = null;
        if (authCode.getScopes().contains("openid")) {
            TokenPort.IdTokenResult idTokenResult = tokenPort.generateIdToken(
                    new TokenPort.IdTokenPayload(
                            user.id().toString(),
                            client.getClientId(),
                            authCode.getNonce(),
                            user.email(),
                            user.emailVerified(),
                            user.firstName(),
                            user.lastName(),
                            orgId == null ? null : orgId.toString()
                    )
            );
            idTokenString = idTokenResult.token();
        }

        String grantedScope = String.join(" ", authCode.getScopes());

        return new ExchangeTokenResult(
                accessTokenResult.token(),
                "Bearer",
                900L,
                rawRefreshToken,
                idTokenString,
                grantedScope.isEmpty() ? null : grantedScope
        );
    }

    private ExchangeTokenResult exchangeRefreshToken(ExchangeTokenCommand command, OAuthClient client) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            throw new InvalidGrantException("refresh_token is required");
        }

        String deviceFingerprint = (command.deviceFingerprint() != null && !command.deviceFingerprint().isBlank())
                ? command.deviceFingerprint().trim()
                : HashingUtils.sha256Hex(command.userAgent() == null ? "oauth-client" : command.userAgent());

        RefreshTokenResponse refreshResponse = refreshTokenHandler.execute(
                new RefreshTokenCommand(command.refreshToken().trim(), deviceFingerprint)
        );

        Optional<Session> currentSession = sessionRepository.findByToken(refreshResponse.refreshToken());
        String idTokenString = null;
        if (currentSession.isPresent()) {
            Long userId = Long.parseLong(currentSession.get().getUserId());
            Optional<UserQueryPort.UserDto> userOpt = userQueryPort.findById(userId);
            if (userOpt.isPresent()) {
                UserQueryPort.UserDto user = userOpt.get();
                TokenPort.IdTokenResult idTokenResult = tokenPort.generateIdToken(
                        new TokenPort.IdTokenPayload(
                                user.id().toString(),
                                client.getClientId(),
                                null,
                                user.email(),
                                user.emailVerified(),
                                user.firstName(),
                                user.lastName(),
                                currentSession.get().getOrganizationId() == null ? null : currentSession.get().getOrganizationId().toString()
                        )
                );
                idTokenString = idTokenResult.token();
            }
        }

        return new ExchangeTokenResult(
                refreshResponse.accessToken(),
                "Bearer",
                900L,
                refreshResponse.refreshToken(),
                idTokenString,
                null
        );
    }
}
