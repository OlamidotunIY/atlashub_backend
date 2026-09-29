package com.atlashub.iam.infrastructure.persistence.entities;

import com.atlashub.iam.domain.valueobject.ApiEnvironment;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "api_keys",
        indexes = {
                @Index(name = "Idx_apikey_org_env", columnList = "organization_id, environment"),
                @Index(name = "Idx_apikey_public_key", columnList = "public_key", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiKeyJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "public_key", nullable = false, unique = true)
    private String publicKey;

    @Column(name = "secret_key_hash", nullable = false)
    private String secretKeyHash;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false)
    private ApiEnvironment environment;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked;

    @Column(name = "last_used_at")
    private ZonedDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private ZonedDateTime revokedAt;

    @Column(name = "revoked_by")
    private Long revokedBy;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    @Version
    private Long version;
}

