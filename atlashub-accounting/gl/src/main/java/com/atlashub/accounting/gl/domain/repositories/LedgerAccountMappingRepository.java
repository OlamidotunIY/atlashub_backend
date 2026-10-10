package com.atlashub.accounting.gl.domain.repositories;

import com.atlashub.accounting.gl.domain.entities.LedgerAccountMapping;
import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface LedgerAccountMappingRepository extends Repository<LedgerAccountMapping> {

    Optional<LedgerAccountMapping> findByOrganizationIdAndSourceSystem(Long organizationId, SourceSystem sourceSystem);

    List<LedgerAccountMapping> findByOrganizationId(Long organizationId);
}
