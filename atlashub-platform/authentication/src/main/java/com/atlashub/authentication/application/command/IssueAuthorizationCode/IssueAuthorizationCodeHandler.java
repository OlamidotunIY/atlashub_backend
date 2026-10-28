package com.atlashub.authentication.application.command.IssueAuthorizationCode;

import com.atlashub.authentication.domain.entities.AuthorizationCode;
import com.atlashub.authentication.domain.entities.OAuthClient;
import com.atlashub.authentication.domain.exceptions.EmailVerificationRequired;
import com.atlashub.authentication.domain.exceptions.InvalidClientException;
import com.atlashub.authentication.domain.exceptions.InvalidPkceChallengeException;
import com.atlashub.authentication.domain.exceptions.OAuthException;
import com.atlashub.authentication.domain.repositories.AuthorizationCodeRepository;
import com.atlashub.authentication.domain.repositories.OAuthClientRepository;
import com.atlashub.authentication.domain.valueobject.CodeChallengeMethod;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.service.HashingUtils;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class IssueAuthorizationCodeHandler extends Command<IssueAuthorizationCodeCommand, IssueAuthorizationCodeResult> {

    private final OAuthClientRepository oauthClientRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final UserQueryPort userQueryPort;

    public IssueAuthorizationCodeHandler(OAuthClientRepository oauthClientRepository,
                                         AuthorizationCodeRepository authorizationCodeRepository,
                                         UserQueryPort userQueryPort) {
        this.oauthClientRepository = oauthClientRepository;
        this.authorizationCodeRepository = authorizationCodeRepository;
        this.userQueryPort = userQueryPort;
    }

    @Override
    @Transactional
    public IssueAuthorizationCodeResult execute(IssueAuthorizationCodeCommand command) {
        if (command.responseType() == null || !"code".equals(command.responseType().trim())) {
            throw new OAuthException("unsupported_response_type", "Only response_type=code is supported");
        }

        if (command.clientId() == null || command.clientId().isBlank()) {
            throw new InvalidClientException("client_id parameter is required");
        }

        OAuthClient client = oauthClientRepository.findByClientId(command.clientId().trim())
                .orElseThrow(() -> new InvalidClientException("Client not found: " + command.clientId()));

        if (!client.isEnabled()) {
            throw new InvalidClientException("Client is disabled: " + command.clientId());
        }

        client.validateRedirectUri(command.redirectUri());

        Set<String> requestedScopes = new HashSet<>();
        if (command.scope() != null && !command.scope().isBlank()) {
            requestedScopes = Arrays.stream(command.scope().trim().split("\\s+"))
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());
            client.validateScopes(requestedScopes);
        }

        if (command.codeChallenge() == null || command.codeChallenge().isBlank()) {
            throw new InvalidPkceChallengeException("code_challenge is required for PKCE");
        }

        if (command.codeChallengeMethod() == null || !"S256".equalsIgnoreCase(command.codeChallengeMethod().trim())) {
            throw new InvalidPkceChallengeException("Only code_challenge_method=S256 is supported");
        }

        if (command.userId() == null) {
            throw new OAuthException("login_required", "User authentication is required to authorize client");
        }

        UserQueryPort.UserDto user = userQueryPort.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.emailVerified()) {
            throw new EmailVerificationRequired();
        }

        String rawCode = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        String codeHash = HashingUtils.sha256Hex(rawCode);

        ZonedDateTime expiresAt = ZonedDateTime.now().plusMinutes(5);
        Long id = authorizationCodeRepository.nextIdentity();

        AuthorizationCode authorizationCode = AuthorizationCode.create(
                id,
                codeHash,
                client.getClientId(),
                user.id(),
                command.redirectUri().trim(),
                requestedScopes,
                command.codeChallenge().trim(),
                CodeChallengeMethod.S256,
                command.nonce(),
                expiresAt
        );

        authorizationCodeRepository.save(authorizationCode);

        return new IssueAuthorizationCodeResult(rawCode, command.redirectUri().trim(), command.state());
    }
}
