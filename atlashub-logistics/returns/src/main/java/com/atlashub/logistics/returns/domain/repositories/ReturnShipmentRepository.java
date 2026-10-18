package com.atlashub.logistics.returns.domain.repositories;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface ReturnShipmentRepository extends Repository<ReturnShipment> {

    Optional<ReturnShipment> findByReturnTrackingNumber(String trackingNumber);

    List<ReturnShipment> findByOrganizationId(Long organizationId);

    Optional<ReturnShipment> findByOriginalShipmentId(Long originalShipmentId);
}
