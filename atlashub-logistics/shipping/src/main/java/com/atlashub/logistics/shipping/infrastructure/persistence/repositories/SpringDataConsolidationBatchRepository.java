package com.atlashub.logistics.shipping.infrastructure.persistence.repositories;

import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ConsolidationBatchJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataConsolidationBatchRepository extends JpaRepository<ConsolidationBatchJpa, Long> {

    Optional<ConsolidationBatchJpa> findByBatchReference(String batchReference);

    @Query("SELECT b FROM ConsolidationBatchJpa b WHERE b.originState = :orig AND b.destinationState = :dest AND b.status = 'OPEN'")
    List<ConsolidationBatchJpa> findOpenBatches(
            @Param("orig") String originState,
            @Param("dest") String destinationState
    );
}
