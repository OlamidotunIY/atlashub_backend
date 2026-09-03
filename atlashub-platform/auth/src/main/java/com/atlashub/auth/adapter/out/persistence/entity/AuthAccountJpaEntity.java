package com.atlashub.auth.adapter.out.persistence.entity;

import com.atlashub.auth.domain.valueobject.AuthProvider;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.auth.domain.valueobject.PrincipalType;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "auth_accounts", indexes = {
        @Index(name = "idx_auth_account_principal", columnList = "principalId, principalType"),
        @Index(name = "idx_auth_account_identifier", columnList = "identifier"),
        @Index(name = "idx_auth_account_secondary_identifier", columnList = "secondaryIdentifier")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuthAccountJpaEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long principalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrincipalType principalType;
    
    @Column(length = 255)
    private String identifier;
    
    @Column(length = 255)
    private String secondaryIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(nullable = true)
    private String credentialHash;

    private String scope;

    @Column(length = 2048)
    private String accessToken;

    @Column(length = 2048)
    private String refreshToken;

    private ZonedDateTime accessTokenExpiresAt;

    private ZonedDateTime refreshTokenExpiresAt;

    private String totpSecret;

    @Column(nullable = false)
    private boolean totpEnabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuthStatus status;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}


