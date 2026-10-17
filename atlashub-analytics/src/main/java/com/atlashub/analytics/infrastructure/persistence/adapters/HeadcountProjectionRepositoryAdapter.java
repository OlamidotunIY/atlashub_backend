package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.HeadcountProjection;
import com.atlashub.analytics.domain.repositories.HeadcountProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.HeadcountProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.HeadcountProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataHeadcountProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class HeadcountProjectionRepositoryAdapter
        extends JpaBaseRepository<HeadcountProjection, HeadcountProjectionJpa>
        implements HeadcountProjectionRepository {

    private final SpringDataHeadcountProjectionRepository springDataRepo;

    public HeadcountProjectionRepositoryAdapter(
            SpringDataHeadcountProjectionRepository springDataRepo,
            HeadcountProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "headcount_projection_seq";
    }

    @Override
    public Optional<HeadcountProjection> findByOrgEnv(Long organizationId, String environment) {
        return springDataRepo.findByOrganizationIdAndEnvironment(organizationId, environment)
                .map(mapper::toDomain);
    }
}
