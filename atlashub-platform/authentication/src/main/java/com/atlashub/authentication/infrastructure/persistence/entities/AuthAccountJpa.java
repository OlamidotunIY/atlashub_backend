package com.atlashub.authentication.infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "accounts",
        indexes = {
                @Index(name = "Idx_account_user_id", columnList = "user_id"),
                @Index(name = "Idx_account_account_id", columnList = "account_id", unique = true),
                @Index(name = "Idx_account_provider", columnList = "provider_id, account_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuthAccountJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "account_id", nullable = false, unique = true)
    private String accountId;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column
    private String accessToken;

    @Column
    private String refreshToken;

    @Column
    private String idToken;

    @Column
    private ZonedDateTime accessTokenExpiresAt;

    @Column
    private ZonedDateTime refreshTokenExpiresAt;

    @Column
    private String scope;

    @Column
    private String password;

    @Column(nullable = false)
    private int failedLoginAttempts;

    @Column
    private ZonedDateTime lockedUntil;

    @Column
    private ZonedDateTime lastLoginAt;

    @Column
    private String lastLoginIp;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
