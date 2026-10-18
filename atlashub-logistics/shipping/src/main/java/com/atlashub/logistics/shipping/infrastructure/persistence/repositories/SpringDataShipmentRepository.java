package com.atlashub.logistics.shipping.infrastructure.persistence.repositories;

import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ShipmentJpa;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataShipmentRepository extends JpaRepository<ShipmentJpa, Long> {

    Optional<ShipmentJpa> findByTrackingNumber(String trackingNumber);

    List<ShipmentJpa> findByOrganizationId(Long organizationId);

    List<ShipmentJpa> findByConsolidationBatchId(Long batchId);

    @Query("SELECT s FROM ShipmentJpa s WHERE s.organizationId = :orgId AND s.status = 'DELIVERED' AND s.deliveredAt >= :start AND s.deliveredAt < :end")
    List<ShipmentJpa> findDeliveredOrdersForMonth(
            @Param("orgId") Long orgId,
            @Param("start") ZonedDateTime start,
            @Param("end") ZonedDateTime end
    );
}
