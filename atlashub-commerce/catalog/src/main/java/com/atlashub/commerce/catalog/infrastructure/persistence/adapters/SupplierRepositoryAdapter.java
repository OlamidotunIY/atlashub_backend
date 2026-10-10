package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.Supplier;
import com.atlashub.commerce.catalog.domain.repositories.SupplierRepository;
import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.SupplierJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.SupplierMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataSupplierRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SupplierRepositoryAdapter
        extends JpaBaseRepository<Supplier, SupplierJpa>
        implements SupplierRepository {

    private final SpringDataSupplierRepository springDataRepo;

    public SupplierRepositoryAdapter(SpringDataSupplierRepository springDataRepo,
                                   SupplierMapper mapper,
                                   DomainSequenceGenerator sequenceGenerator,
                                   DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_supplier_seq";
    }

    @Override
    public List<Supplier> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Supplier> findByOrganizationIdAndStatus(Long organizationId, SupplierStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
