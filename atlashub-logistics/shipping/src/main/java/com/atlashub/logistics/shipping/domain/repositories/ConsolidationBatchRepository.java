package com.atlashub.logistics.shipping.domain.repositories;

import com.atlashub.logistics.shipping.domain.entities.ConsolidationBatch;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface ConsolidationBatchRepository extends Repository<ConsolidationBatch> {

    Optional<ConsolidationBatch> findByBatchReference(String batchReference);

    List<ConsolidationBatch> findOpenBatches(String originState, String destinationState);
}
