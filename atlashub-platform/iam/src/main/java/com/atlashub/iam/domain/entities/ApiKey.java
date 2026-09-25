package com.atlashub.iam.domain.entities;

import com.atlashub.iam.domain.valueobject.ApiEnvironment;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class ApiKey extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private String publicKey;
    private String secretKeyHash;
    private String name;
    private ApiEnvironment environment;
    private Boolean isRevoked;
    private ZonedDateTime lastUsedAt;
    private ZonedDateTime revokedAt;
    private Long revokedBy;
    private final ZonedDateTime createdAt;

    public ApiKey(Long id, Long organizationId, String publicKey, String secretKeyHash, String name, ApiEnvironment environment, Boolean isRevoked, ZonedDateTime lastUsedAt, ZonedDateTime revokedAt, Long revokedBy, ZonedDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.publicKey = publicKey;
        this.secretKeyHash = secretKeyHash;
        this.name = name;
        this.environment = environment;
        this.isRevoked = isRevoked;
        this.lastUsedAt = lastUsedAt;
        this.revokedAt = revokedAt;
        this.revokedBy = revokedBy;
        this.createdAt = createdAt;
    }

    public static ApiKey create(Long id, Long organizationId, String publicKey, String secretKeyHash, String name, ApiEnvironment environment) {
        ZonedDateTime now = ZonedDateTime.now();

        return new ApiKey(id, organizationId, publicKey, secretKeyHash, name, environment, false, null, null, null, now);
    }

    public void revoke(Long revokedByUserId) {
        if (this.isRevoked) {
            // already revoked
            return;
        }

        this.isRevoked = true;
        this.revokedBy = revokedByUserId;
        this.revokedAt = ZonedDateTime.now();
    }

    public void recordUsage() {
        this.lastUsedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
