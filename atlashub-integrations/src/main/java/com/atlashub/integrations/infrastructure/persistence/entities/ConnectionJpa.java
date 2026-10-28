package com.atlashub.integrations.infrastructure.persistence.entities;

import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.valueobject.Provider;
import jakarta.persistence.*;

@Entity
@Table(name = "integration_connections", uniqueConstraints = @UniqueConstraint(name = "uk_integration_org_provider_country", columnNames = {"organization_id", "provider", "merchant_country"}))
public class ConnectionJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "organization_id", nullable = false)
    public Long organizationId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Provider provider;
    @Column(name = "merchant_country", nullable = false, length = 2)
    public String merchantCountry;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ProviderConnection.Status status;
    @Version
    public Long version;
}
