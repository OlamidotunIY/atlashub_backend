package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.Vendor;
import com.atlashub.commerce.catalog.domain.repositories.VendorRepository;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.VendorJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.VendorMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataVendorRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class VendorRepositoryAdapter
        extends JpaBaseRepository<Vendor, VendorJpa>
        implements VendorRepository {

    private final SpringDataVendorRepository springDataRepo;

    public VendorRepositoryAdapter(SpringDataVendorRepository springDataRepo,
                                 VendorMapper mapper,
                                 DomainSequenceGenerator sequenceGenerator,
                                 DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_vendor_seq";
    }

    @Override
    public List<Vendor> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Vendor> findByOrganizationIdAndStatus(Long organizationId, VendorStatus status) {
        return springDataRepo.findByOrganizationIdAndStatus(organizationId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Vendor> findByOrganizationIdAndUserId(Long organizationId, Long userId) {
        return springDataRepo.findByOrganizationIdAndUserId(organizationId, userId)
                .map(mapper::toDomain);
    }
}
