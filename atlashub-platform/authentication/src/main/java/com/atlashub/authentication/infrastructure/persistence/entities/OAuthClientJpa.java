package com.atlashub.authentication.infrastructure.persistence.entities;

import com.atlashub.authentication.domain.valueobject.OAuthClientType;
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
        name = "oauth_clients",
        indexes = {
                @Index(name = "Idx_oauth_client_id", columnList = "client_id", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OAuthClientJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false)
    private OAuthClientType clientType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "redirect_uris", columnDefinition = "json", nullable = false)
    private Set<String> redirectUris;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_scopes", columnDefinition = "json")
    private Set<String> allowedScopes;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
