package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.FinancingBehaviourProjection;
import com.atlashub.analytics.domain.repositories.FinancingBehaviourProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.FinancingBehaviourProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.FinancingBehaviourProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataFinancingBehaviourProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FinancingBehaviourProjectionRepositoryAdapter
        extends JpaBaseRepository<FinancingBehaviourProjection, FinancingBehaviourProjectionJpa>
        implements FinancingBehaviourProjectionRepository {

    private final SpringDataFinancingBehaviourProjectionRepository springDataRepo;

    public FinancingBehaviourProjectionRepositoryAdapter(
            SpringDataFinancingBehaviourProjectionRepository springDataRepo,
            FinancingBehaviourProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "financing_behaviour_projection_seq";
    }

    @Override
    public Optional<FinancingBehaviourProjection> findByOrgEnvAndContextKey(Long organizationId, String environment, String paymentContextKey) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndPaymentContextKey(organizationId, environment, paymentContextKey)
                .map(mapper::toDomain);
    }
}
