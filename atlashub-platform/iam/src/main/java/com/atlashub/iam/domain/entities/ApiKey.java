package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.events.ApiKeyRevokedEvent;
import com.atlashub.iam.domain.exception.ApiKeyAlreadyRevokedException;
import com.atlashub.iam.domain.valueobject.ApiEnvironment;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class ApiKey extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String publicKey;
    private final String secretKeyCiphertext;
    private final String name;
    private final ApiEnvironment environment;
    private final Long boundRoleId;
    private Boolean revoked;
    private ZonedDateTime lastUsedAt;
    private ZonedDateTime revokedAt;
    private Long revokedBy;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public ApiKey(Long id, Long organizationId, String publicKey, String secretKeyCiphertext, String name,
                  ApiEnvironment environment, Long boundRoleId, Boolean revoked, ZonedDateTime lastUsedAt,
                  ZonedDateTime revokedAt, Long revokedBy, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.publicKey = publicKey;
        this.secretKeyCiphertext = secretKeyCiphertext;
        this.name = name;
        this.environment = environment;
        this.boundRoleId = boundRoleId;
        this.revoked = revoked;
        this.lastUsedAt = lastUsedAt;
        this.revokedAt = revokedAt;
        this.revokedBy = revokedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ApiKey create(Long id, Long organizationId, String publicKey, String secretKeyCiphertext,
                                String name, ApiEnvironment environment, Long boundRoleId) {
        if (id == null || organizationId == null || boundRoleId == null || environment == null) {
            throw new IllegalArgumentException("API key identity, organization, environment, and role are required");
        }
        ZonedDateTime now = ZonedDateTime.now();

        return new ApiKey(id, organizationId, publicKey, secretKeyCiphertext, name, environment,
                boundRoleId, false, null, null, null, now, now);
    }

    public void revoke(Long revokedByUserId) {
        if (Boolean.TRUE.equals(this.revoked)) {
            throw new ApiKeyAlreadyRevokedException("Api key with ID " + this.id + " is already revoked.");
        }

        this.revoked = true;
        this.revokedBy = revokedByUserId;
        this.revokedAt = ZonedDateTime.now();
        this.touch();
        
        registerEvent(new ApiKeyRevokedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.revokedAt,
                CorrelationId.getOrCreate(),
                new ApiKeyRevokedEvent.Payload(this.organizationId, this.publicKey, revokedByUserId)
        ));
    }

    public void recordUsage() {
        this.lastUsedAt = ZonedDateTime.now();
        this.touch();
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
