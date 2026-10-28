package com.atlashub.integrations.application.services;

import com.atlashub.integrations.application.port.StripeOAuthPort;
import com.atlashub.integrations.domain.entities.AuthorizationSession;
import com.atlashub.integrations.domain.repositories.AuthorizationSessionRepository;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class StripeAuthorizationService {
    public record StartResult(String authorizationUrl, ZonedDateTime expiresAt) {}

    private final AuthorizationSessionRepository sessions;
    private final StripeOAuthPort oauth;
    private final ConnectionService connections;
    private final SecureRandom random = new SecureRandom();

    public StripeAuthorizationService(AuthorizationSessionRepository sessions, StripeOAuthPort oauth,
                                      ConnectionService connections) {
        this.sessions = sessions;
        this.oauth = oauth;
        this.connections = connections;
    }

    @Transactional
    public StartResult start(Long organizationId, ApiEnvironment environment, String merchantCountry) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        ZonedDateTime expiresAt = ZonedDateTime.now().plusMinutes(10);
        String country = merchantCountry.toUpperCase(Locale.ROOT);
        sessions.save(new AuthorizationSession(null, hash(state), organizationId, environment,
                country, expiresAt, false));
        return new StartResult(oauth.createAuthorizationUrl(environment, country, state), expiresAt);
    }

    public ConnectionService.ConnectedAuthorization callback(String state, String code) {
        if (state == null || code == null || state.isBlank() || code.isBlank()) {
            throw new IllegalArgumentException("Stripe callback is incomplete");
        }
        AuthorizationSession session = requireUsable(state);
        StripeOAuthPort.Token token = oauth.exchange(session.environment(), code);
        if (token.live() != (session.environment() == ApiEnvironment.LIVE)) {
            throw new IllegalStateException("Stripe returned an authorization for another environment");
        }
        var pending = connections.connectCredential(session.organizationId(), Provider.STRIPE,
                session.merchantCountry(), token.accessToken());
        sessions.save(session.consume());
        return connections.verify(session.organizationId(), pending.authorization().id());
    }

    @Transactional
    public void denied(String state) {
        sessions.save(requireUsable(state).consume());
    }

    private AuthorizationSession requireUsable(String state) {
        if (state == null || state.isBlank()) throw new IllegalArgumentException("Stripe authorization state is missing");
        AuthorizationSession session = sessions.findByStateHash(hash(state))
                .orElseThrow(() -> new IllegalArgumentException("Stripe authorization state is invalid"));
        if (session.consumed() || session.expiresAt().isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("Stripe authorization state has expired or was used");
        }
        return session;
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
