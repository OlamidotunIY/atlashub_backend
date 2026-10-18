package com.atlashub.logistics.shipping.infrastructure.persistence.adapters;

import com.atlashub.logistics.shipping.domain.entities.ConsolidationBatch;
import com.atlashub.logistics.shipping.domain.repositories.ConsolidationBatchRepository;
import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ConsolidationBatchJpa;
import com.atlashub.logistics.shipping.infrastructure.persistence.mappers.ConsolidationBatchMapper;
import com.atlashub.logistics.shipping.infrastructure.persistence.repositories.SpringDataConsolidationBatchRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.infrastructure.persistence.repository.JpaBaseRepository;
import com.atlashub.shared.infrastructure.service.DomainSequenceGenerator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ConsolidationBatchRepositoryAdapter
        extends JpaBaseRepository<ConsolidationBatch, ConsolidationBatchJpa>
        implements ConsolidationBatchRepository {

    private final SpringDataConsolidationBatchRepository springDataRepo;

    public ConsolidationBatchRepositoryAdapter(
            SpringDataConsolidationBatchRepository springDataRepo,
            ConsolidationBatchMapper mapper,
            DomainSequenceGenerator sequenceGenerator,
            DomainEventPublisher eventPublisher
    ) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "logistics_batch_seq";
    }

    @Override
    public Optional<ConsolidationBatch> findByBatchReference(String batchReference) {
        return springDataRepo.findByBatchReference(batchReference).map(mapper::toDomain);
    }

    @Override
    public List<ConsolidationBatch> findOpenBatches(String originState, String destinationState) {
        return springDataRepo.findOpenBatches(originState, destinationState).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
