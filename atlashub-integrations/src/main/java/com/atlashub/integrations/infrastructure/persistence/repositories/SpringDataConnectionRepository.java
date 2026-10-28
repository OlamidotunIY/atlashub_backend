package com.atlashub.integrations.infrastructure.persistence.repositories;

import com.atlashub.integrations.infrastructure.persistence.entities.ConnectionJpa;
import com.atlashub.integrations.domain.valueobject.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataConnectionRepository extends JpaRepository<ConnectionJpa, Long> {
    List<ConnectionJpa> findByOrganizationId(Long organizationId);
    Optional<ConnectionJpa> findByOrganizationIdAndProviderAndMerchantCountry(Long organizationId, Provider provider,
                                                                               String merchantCountry);
}
