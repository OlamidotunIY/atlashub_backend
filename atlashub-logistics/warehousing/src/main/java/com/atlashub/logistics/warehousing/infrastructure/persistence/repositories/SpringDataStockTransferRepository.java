package com.atlashub.logistics.warehousing.infrastructure.persistence.repositories;

import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.StockTransferJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataStockTransferRepository extends JpaRepository<StockTransferJpa, Long> {

    Optional<StockTransferJpa> findByShipmentId(Long shipmentId);

    List<StockTransferJpa> findByOrganizationId(Long organizationId);

    @Query("SELECT st FROM StockTransferJpa st WHERE st.sourceId = :sourceId OR st.destinationId = :destinationId")
    List<StockTransferJpa> findBySourceIdOrDestinationId(@Param("sourceId") Long sourceId, @Param("destinationId") Long destinationId);
}
