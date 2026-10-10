package com.atlashub.commerce.storefront.infrastructure.persistence.adapters;

import com.atlashub.commerce.storefront.domain.entities.Till;
import com.atlashub.commerce.storefront.domain.repositories.TillRepository;
import com.atlashub.commerce.storefront.domain.valueobject.TillStatus;
import com.atlashub.commerce.storefront.infrastructure.persistence.entities.TillJpa;
import com.atlashub.commerce.storefront.infrastructure.persistence.mappers.TillMapper;
import com.atlashub.commerce.storefront.infrastructure.persistence.repositories.SpringDataTillRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TillRepositoryAdapter
        extends JpaBaseRepository<Till, TillJpa>
        implements TillRepository {

    private final SpringDataTillRepository springDataRepo;

    public TillRepositoryAdapter(
            SpringDataTillRepository springDataRepo,
            TillMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_till_seq";
    }

    @Override
    public Optional<Till> findActiveTillByOutletId(Long outletId) {
        return springDataRepo.findByOutletIdAndStatus(outletId, TillStatus.OPEN)
                .map(mapper::toDomain);
    }

    @Override
    public List<Till> findByOutletId(Long outletId) {
        return springDataRepo.findByOutletId(outletId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
