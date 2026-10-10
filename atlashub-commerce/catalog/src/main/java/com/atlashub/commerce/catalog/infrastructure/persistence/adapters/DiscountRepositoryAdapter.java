package com.atlashub.commerce.catalog.infrastructure.persistence.adapters;

import com.atlashub.commerce.catalog.domain.entities.Discount;
import com.atlashub.commerce.catalog.domain.repositories.DiscountRepository;
import com.atlashub.commerce.catalog.infrastructure.persistence.entities.DiscountJpa;
import com.atlashub.commerce.catalog.infrastructure.persistence.mappers.DiscountMapper;
import com.atlashub.commerce.catalog.infrastructure.persistence.repositories.SpringDataDiscountRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DiscountRepositoryAdapter
        extends JpaBaseRepository<Discount, DiscountJpa>
        implements DiscountRepository {

    private final SpringDataDiscountRepository springDataRepo;

    public DiscountRepositoryAdapter(SpringDataDiscountRepository springDataRepo,
                                   DiscountMapper mapper,
                                   DomainSequenceGenerator sequenceGenerator,
                                   DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "commerce_discount_seq";
    }

    @Override
    public Optional<Discount> findByOrganizationIdAndName(Long organizationId, String name) {
        return springDataRepo.findByOrganizationIdAndName(organizationId, name)
                .map(mapper::toDomain);
    }

    @Override
    public List<Discount> findActiveByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
