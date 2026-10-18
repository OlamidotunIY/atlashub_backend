package com.atlashub.logistics.shipping.domain.repositories;

import com.atlashub.logistics.shipping.domain.entities.ShipmentMilestoneLog;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;

public interface ShipmentMilestoneLogRepository extends Repository<ShipmentMilestoneLog> {

    List<ShipmentMilestoneLog> findByShipmentIdOrderByOccurredAtAsc(Long shipmentId);

    List<ShipmentMilestoneLog> findByTrackingNumberOrderByOccurredAtAsc(String trackingNumber);
}
