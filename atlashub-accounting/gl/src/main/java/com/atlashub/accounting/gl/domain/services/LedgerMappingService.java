package com.atlashub.accounting.gl.domain.services;

import com.atlashub.accounting.gl.domain.entities.LedgerAccountMapping;
import com.atlashub.accounting.gl.domain.exceptions.LedgerMappingNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.LedgerAccountMappingRepository;
import com.atlashub.accounting.gl.domain.valueobject.SourceSystem;

import java.util.Objects;

public class LedgerMappingService {

    private final LedgerAccountMappingRepository repository;

    public LedgerMappingService(LedgerAccountMappingRepository repository) {
        this.repository = Objects.requireNonNull(repository, "LedgerAccountMappingRepository must not be null");
    }

    public LedgerAccountMapping resolveMapping(Long organizationId, SourceSystem sourceSystem) {
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(sourceSystem, "SourceSystem must not be null");

        return repository.findByOrganizationIdAndSourceSystem(organizationId, sourceSystem)
                .orElseThrow(() -> new LedgerMappingNotFoundException(sourceSystem.name()));
    }
}
