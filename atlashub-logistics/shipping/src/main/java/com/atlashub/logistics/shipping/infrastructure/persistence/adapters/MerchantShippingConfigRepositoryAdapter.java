package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingConfig;
import com.atlashub.logistics.shipping.domain.repositories.MerchantShippingConfigRepository;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.MerchantShippingConfigJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.mappers.MerchantShippingConfigMapper;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataMerchantShippingConfigRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MerchantShippingConfigRepositoryAdapter
        extends JpaBaseRepository<MerchantShippingConfig, MerchantShippingConfigJpa>
        implements MerchantShippingConfigRepository {

    private final SpringDataMerchantShippingConfigRepository springDataRepo;

    public MerchantShippingConfigRepositoryAdapter(
            SpringDataMerchantShippingConfigRepository springDataRepo,
            MerchantShippingConfigMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "logistics_merchant_config_seq";
    }

    @Override
    public Optional<MerchantShippingConfig> findByOrganizationId(Long organizationId) {
        return springDataRepo.findByOrganizationId(organizationId).map(mapper::toDomain);
    }
}
