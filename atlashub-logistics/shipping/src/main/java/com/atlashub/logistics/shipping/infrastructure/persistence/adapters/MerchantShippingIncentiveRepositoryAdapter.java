package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingIncentive;
import com.atlashub.logistics.shipping.domain.repositories.MerchantShippingIncentiveRepository;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.MerchantShippingIncentiveJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.mappers.MerchantShippingIncentiveMapper;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataMerchantShippingIncentiveRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MerchantShippingIncentiveRepositoryAdapter
        extends JpaBaseRepository<MerchantShippingIncentive, MerchantShippingIncentiveJpa>
        implements MerchantShippingIncentiveRepository {

    private final SpringDataMerchantShippingIncentiveRepository springDataRepo;

    public MerchantShippingIncentiveRepositoryAdapter(
            SpringDataMerchantShippingIncentiveRepository springDataRepo,
            MerchantShippingIncentiveMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "logistics_incentive_seq";
    }

    @Override
    public Optional<MerchantShippingIncentive> findByOrganizationIdAndBillingYearMonth(Long organizationId, String billingYearMonth) {
        return springDataRepo.findByOrganizationIdAndBillingYearMonth(organizationId, billingYearMonth)
                .map(mapper::toDomain);
    }

    @Override
    public List<MerchantShippingIncentive> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
