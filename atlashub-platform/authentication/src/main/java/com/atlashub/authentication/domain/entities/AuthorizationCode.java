package com.atlashub.authentication.domain.entities;

import com.atlashub.authentication.domain.exceptions.AuthenticationInvariantException;
import com.atlashub.authentication.domain.exceptions.InvalidGrantException;
import com.atlashub.authentication.domain.exceptions.InvalidRedirectUriException;
import com.atlashub.authentication.domain.valueobject.CodeChallengeMethod;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
public class AuthorizationCode extends AggregateRoot<Long> {

    private final Long id;
    private final String codeHash;
    private final String clientId;
    private final Long userId;
    private final String redirectUri;
    private final Set<String> scopes;
    private final String codeChallenge;
    private final CodeChallengeMethod codeChallengeMethod;
    private final String nonce;
    private final ZonedDateTime expiresAt;
    private boolean used;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public AuthorizationCode(Long id, String codeHash, String clientId, Long userId,
                             String redirectUri, Set<String> scopes, String codeChallenge,
                             CodeChallengeMethod codeChallengeMethod, String nonce,
                             ZonedDateTime expiresAt, boolean used,
                             ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.codeHash = codeHash;
        this.clientId = clientId;
        this.userId = userId;
        this.redirectUri = redirectUri;
        this.scopes = scopes == null ? new HashSet<>() : new HashSet<>(scopes);
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.nonce = nonce;
        this.expiresAt = expiresAt;
        this.used = used;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AuthorizationCode create(Long id, String codeHash, String clientId, Long userId,
                                           String redirectUri, Set<String> scopes, String codeChallenge,
                                           CodeChallengeMethod codeChallengeMethod, String nonce,
                                           ZonedDateTime expiresAt) {
        if (id == null) {
            throw new AuthenticationInvariantException("Authorization code id cannot be null");
        }
        if (codeHash == null || codeHash.isBlank()) {
            throw new AuthenticationInvariantException("Authorization codeHash is required");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new AuthenticationInvariantException("Authorization clientId is required");
        }
        if (userId == null) {
            throw new AuthenticationInvariantException("Authorization userId is required");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new AuthenticationInvariantException("Authorization redirectUri is required");
        }
        if (codeChallenge == null || codeChallenge.isBlank()) {
            throw new AuthenticationInvariantException("PKCE codeChallenge is required");
        }
        if (codeChallengeMethod == null) {
            throw new AuthenticationInvariantException("PKCE codeChallengeMethod is required");
        }
        if (expiresAt == null) {
            throw new AuthenticationInvariantException("Authorization expiresAt is required");
        }
        ZonedDateTime now = ZonedDateTime.now();
        return new AuthorizationCode(id, codeHash.trim(), clientId.trim(), userId,
                redirectUri.trim(), scopes, codeChallenge.trim(), codeChallengeMethod,
                nonce, expiresAt, false, now, now);
    }

    public void markAsUsed() {
        if (this.used) {
            throw new InvalidGrantException("Authorization code has already been used");
        }
        this.used = true;
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiresAt);
    }

    public void validateRedirectUri(String uri) {
        if (uri == null || !this.redirectUri.equals(uri.trim())) {
            throw new InvalidRedirectUriException("Redirect URI mismatch: expected '" + this.redirectUri + "', got '" + uri + "'");
        }
    }

    public void validateClientId(String clientId) {
        if (clientId == null || !this.clientId.equals(clientId.trim())) {
            throw new InvalidGrantException("Client ID mismatch: expected '" + this.clientId + "', got '" + clientId + "'");
        }
    }

    public Set<String> getScopes() {
        return Collections.unmodifiableSet(scopes);
    }

    @Override
    public Long getId() {
        return id;
    }
}
