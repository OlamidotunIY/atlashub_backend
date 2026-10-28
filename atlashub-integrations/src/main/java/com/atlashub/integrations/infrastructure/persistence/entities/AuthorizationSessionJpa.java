package com.atlashub.integrations.infrastructure.persistence.entities;

import com.atlashub.shared.application.security.ApiEnvironment;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "integration_authorization_sessions", indexes = @Index(name = "idx_integration_auth_state", columnList = "state_hash", unique = true))
public class AuthorizationSessionJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "state_hash", nullable = false, length = 64)
    public String stateHash;
    @Column(name = "organization_id", nullable = false)
    public Long organizationId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ApiEnvironment environment;
    @Column(name = "merchant_country", nullable = false, length = 2)
    public String merchantCountry;
    @Column(name = "expires_at", nullable = false)
    public ZonedDateTime expiresAt;
    @Column(nullable = false)
    public boolean consumed;
    @Version
    public Long version;
}
