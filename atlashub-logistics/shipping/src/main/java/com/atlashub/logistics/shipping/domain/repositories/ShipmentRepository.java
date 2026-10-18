package com.atlashub.logistics.shipping.domain.repositories;

import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.shared.domain.repository.Repository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends Repository<Shipment> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByOrganizationId(Long organizationId);

    List<Shipment> findByConsolidationBatchId(Long batchId);

    List<Shipment> findDeliveredOrdersForMonth(Long organizationId, ZonedDateTime start, ZonedDateTime end);
}
