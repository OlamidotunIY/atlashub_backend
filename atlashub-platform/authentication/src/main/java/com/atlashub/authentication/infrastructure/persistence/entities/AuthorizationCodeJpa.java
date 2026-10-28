package com.atlashub.authentication.infrastructure.persistence.entities;

import com.atlashub.authentication.domain.valueobject.CodeChallengeMethod;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Table(
        name = "oauth_authorization_codes",
        indexes = {
                @Index(name = "Idx_oauth_code_hash", columnList = "code_hash", unique = true),
                @Index(name = "Idx_oauth_code_client_id", columnList = "client_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuthorizationCodeJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "code_hash", nullable = false, unique = true)
    private String codeHash;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "redirect_uri", nullable = false)
    private String redirectUri;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scopes", columnDefinition = "json")
    private Set<String> scopes;

    @Column(name = "code_challenge", nullable = false)
    private String codeChallenge;

    @Enumerated(EnumType.STRING)
    @Column(name = "code_challenge_method", nullable = false)
    private CodeChallengeMethod codeChallengeMethod;

    @Column(name = "nonce")
    private String nonce;

    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
