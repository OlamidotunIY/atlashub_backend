package com.atlashub.accounting.gl.infrastructure.persistence.repositories;

import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.LedgerAccountMappingJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataLedgerAccountMappingRepository extends JpaRepository<LedgerAccountMappingJpa, Long> {

    Optional<LedgerAccountMappingJpa> findByOrganizationIdAndSourceSystem(Long organizationId, SourceSystem sourceSystem);

    List<LedgerAccountMappingJpa> findByOrganizationId(Long organizationId);
}
