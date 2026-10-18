package com.atlashub.pay.controls.infrastructure.persistence.adapters;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.pay.controls.domain.valueobject.PolicyStatus;
import com.atlashub.pay.controls.infrastructure.persistence.entities.ControlledFundsPolicyJpa;
import com.atlashub.pay.controls.infrastructure.persistence.mappers.ControlledFundsPolicyMapper;
import com.atlashub.pay.controls.infrastructure.persistence.repositories.SpringDataControlledFundsPolicyRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ControlledFundsPolicyRepositoryAdapter
        extends JpaBaseRepository<ControlledFundsPolicy, ControlledFundsPolicyJpa>
        implements ControlledFundsPolicyRepository {

    private final SpringDataControlledFundsPolicyRepository springDataRepo;

    public ControlledFundsPolicyRepositoryAdapter(
            SpringDataControlledFundsPolicyRepository springDataRepo,
            ControlledFundsPolicyMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "pay_controlled_funds_policy_seq";
    }

    @Override
    public Optional<ControlledFundsPolicy> findActiveByFacilityId(Long facilityId) {
        return springDataRepo.findByFacilityIdAndStatus(facilityId, PolicyStatus.ACTIVE)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ControlledFundsPolicy> findByFacilityIdAndPolicyVersion(Long facilityId, String policyVersion) {
        return springDataRepo.findByFacilityIdAndPolicyVersion(facilityId, policyVersion)
                .map(mapper::toDomain);
    }
}
