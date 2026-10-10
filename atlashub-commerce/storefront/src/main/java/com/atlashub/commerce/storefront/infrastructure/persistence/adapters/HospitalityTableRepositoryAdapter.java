package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.HospitalityTable;
import com.atlashub.commerce.storefront.domain.repositories.HospitalityTableRepository;
import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.HospitalityTableJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.HospitalityTableMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataHospitalityTableRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class HospitalityTableRepositoryAdapter
        extends JpaBaseRepository<HospitalityTable, HospitalityTableJpa>
        implements HospitalityTableRepository {

    private final SpringDataHospitalityTableRepository springDataRepo;

    public HospitalityTableRepositoryAdapter(
            SpringDataHospitalityTableRepository springDataRepo,
            HospitalityTableMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_hospitality_table_seq";
    }

    @Override
    public Optional<HospitalityTable> findByOutletIdAndTableNumber(Long outletId, String tableNumber) {
        return springDataRepo.findByOutletIdAndTableNumber(outletId, tableNumber)
                .map(mapper::toDomain);
    }

    @Override
    public List<HospitalityTable> findByOutletId(Long outletId) {
        return springDataRepo.findByOutletId(outletId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<HospitalityTable> findByOutletIdAndStatus(Long outletId, TableStatus status) {
        return springDataRepo.findByOutletIdAndStatus(outletId, status).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
