package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.domain.entities.TransactionVolumeProjection;
import com.atlashub.analytics.domain.repositories.TransactionVolumeProjectionRepository;
import com.atlashub.analytics.infrastructure.persistence.entities.TransactionVolumeProjectionJpa;
import com.atlashub.analytics.infrastructure.persistence.mappers.TransactionVolumeProjectionMapper;
import com.atlashub.analytics.infrastructure.persistence.repositories.SpringDataTransactionVolumeProjectionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TransactionVolumeProjectionRepositoryAdapter
        extends JpaBaseRepository<TransactionVolumeProjection, TransactionVolumeProjectionJpa>
        implements TransactionVolumeProjectionRepository {

    private final SpringDataTransactionVolumeProjectionRepository springDataRepo;

    public TransactionVolumeProjectionRepositoryAdapter(
            SpringDataTransactionVolumeProjectionRepository springDataRepo,
            TransactionVolumeProjectionMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "tx_volume_projection_seq";
    }

    @Override
    public Optional<TransactionVolumeProjection> findByOrgEnvAndMonth(Long organizationId, String environment, String metricMonth) {
        return springDataRepo.findByOrganizationIdAndEnvironmentAndMetricMonth(organizationId, environment, metricMonth)
                .map(mapper::toDomain);
    }

    @Override
    public List<TransactionVolumeProjection> findAllByMonth(String metricMonth) {
        return springDataRepo.findAllByMonth(metricMonth)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
