package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.BusinessFeatureSnapshot;
import com.atlashub.analytics.domain.repositories.BusinessFeatureSnapshotRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.BusinessFeatureSnapshotJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.BusinessFeatureSnapshotMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataBusinessFeatureSnapshotRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BusinessFeatureSnapshotRepositoryAdapter
        extends JpaBaseRepository<BusinessFeatureSnapshot, BusinessFeatureSnapshotJpa>
        implements BusinessFeatureSnapshotRepository {

    private final SpringDataBusinessFeatureSnapshotRepository springDataRepo;

    public BusinessFeatureSnapshotRepositoryAdapter(
            SpringDataBusinessFeatureSnapshotRepository springDataRepo,
            BusinessFeatureSnapshotMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "business_feature_snapshot_seq";
    }

    @Override
    public Optional<BusinessFeatureSnapshot> findLatestByOrgEnv(Long organizationId, String environment, String schemaVersion) {
        return springDataRepo.findLatest(organizationId, environment, schemaVersion)
                .map(mapper::toDomain);
    }
}
