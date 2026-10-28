package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.exceptions.AuthenticationInvariantException;
import com.atlashub.authentication.domain.exceptions.InvalidClientException;
import com.atlashub.authentication.domain.exceptions.InvalidRedirectUriException;
import com.atlashub.authentication.domain.exceptions.InvalidScopeException;
import com.atlashub.authentication.domain.valueobject.OAuthClientType;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
public class OAuthClient extends AggregateRoot<Long> {

    private final Long id;
    private final String clientId;
    private String clientSecret;
    private String clientName;
    private OAuthClientType clientType;
    private Set<String> redirectUris;
    private Set<String> allowedScopes;
    private boolean enabled;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public OAuthClient(Long id, String clientId, String clientSecret, String clientName,
                       OAuthClientType clientType, Set<String> redirectUris,
                       Set<String> allowedScopes, boolean enabled,
                       ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientName = clientName;
        this.clientType = clientType;
        this.redirectUris = redirectUris == null ? new HashSet<>() : new HashSet<>(redirectUris);
        this.allowedScopes = allowedScopes == null ? new HashSet<>() : new HashSet<>(allowedScopes);
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OAuthClient create(Long id, String clientId, String clientSecret, String clientName,
                                     OAuthClientType clientType, Set<String> redirectUris,
                                     Set<String> allowedScopes) {
        if (id == null) {
            throw new AuthenticationInvariantException("Client ID cannot be null");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new AuthenticationInvariantException("OAuth clientId is required");
        }
        if (clientName == null || clientName.isBlank()) {
            throw new AuthenticationInvariantException("OAuth clientName is required");
        }
        if (clientType == null) {
            throw new AuthenticationInvariantException("OAuth clientType is required");
        }
        if (clientType == OAuthClientType.CONFIDENTIAL && (clientSecret == null || clientSecret.isBlank())) {
            throw new AuthenticationInvariantException("Confidential client requires client secret");
        }
        if (redirectUris == null || redirectUris.isEmpty()) {
            throw new AuthenticationInvariantException("At least one redirect URI is required");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new OAuthClient(id, clientId.trim(), clientSecret, clientName.trim(),
                clientType, redirectUris, allowedScopes, true, now, now);
    }

    public void validateRedirectUri(String uri) {
        if (uri == null || uri.isBlank()) {
            throw new InvalidRedirectUriException("Redirect URI cannot be empty");
        }
        String normalized = uri.trim();
        if (!redirectUris.contains(normalized)) {
            throw new InvalidRedirectUriException("Redirect URI '" + normalized + "' is not registered for client '" + clientId + "'");
        }
    }

    public void validateScopes(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return;
        }
        for (String scope : scopes) {
            if (!allowedScopes.contains(scope)) {
                throw new InvalidScopeException("Scope '" + scope + "' is not allowed for client '" + clientId + "'");
            }
        }
    }

    public void validateSecret(String rawSecret, PasswordEncoderPort encoder) {
        if (clientType == OAuthClientType.PUBLIC) {
            return;
        }
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new InvalidClientException("Client secret is required for confidential client");
        }
        if (encoder == null || !encoder.matches(rawSecret, clientSecret)) {
            throw new InvalidClientException("Invalid client secret");
        }
    }

    public boolean isPublic() {
        return clientType == OAuthClientType.PUBLIC;
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = ZonedDateTime.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = ZonedDateTime.now();
    }

    public Set<String> getRedirectUris() {
        return Collections.unmodifiableSet(redirectUris);
    }

    public Set<String> getAllowedScopes() {
        return Collections.unmodifiableSet(allowedScopes);
    }

    @Override
    public Long getId() {
        return id;
    }
}
