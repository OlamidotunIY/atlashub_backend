package com.atlashub.integrations.infrastructure.persistence.entities;

import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.shared.application.security.ApiEnvironment;
import jakarta.persistence.*;

@Entity
@Table(name = "integration_authorizations", uniqueConstraints = @UniqueConstraint(name = "uk_integration_connection_environment", columnNames = {"connection_id", "environment"}), indexes = @Index(name = "idx_integration_authorization_connection", columnList = "connection_id"))
public class AuthorizationJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "connection_id", nullable = false)
    public Long connectionId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ApiEnvironment environment;
    @Column(name = "external_merchant_id")
    public String externalMerchantId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ProviderAuthorization.Status status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ProviderAuthorization.Health health;
    @Column(name = "active_capabilities", length = 1024)
    public String activeCapabilities;
    @Column(name = "credential_reference", nullable = false)
    public String credentialReference;
    @Version
    public Long version;
}
