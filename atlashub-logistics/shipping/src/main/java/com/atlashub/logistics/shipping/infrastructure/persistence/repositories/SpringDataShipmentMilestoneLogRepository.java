package com.atlashub.logistics.shipping.infrastructure.persistence.repositories;

import com.atlashub.logistics.shipping.infrastructure.persistence.entities.ShipmentMilestoneLogJpa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataShipmentMilestoneLogRepository extends JpaRepository<ShipmentMilestoneLogJpa, Long> {

    List<ShipmentMilestoneLogJpa> findByShipmentIdOrderByOccurredAtAsc(Long shipmentId);

    List<ShipmentMilestoneLogJpa> findByTrackingNumberOrderByOccurredAtAsc(String trackingNumber);
}
