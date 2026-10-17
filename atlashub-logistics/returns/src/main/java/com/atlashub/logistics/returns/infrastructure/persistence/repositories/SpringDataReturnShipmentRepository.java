package com.atlashub.logistics.returns.infrastructure.persistence.repositories;

import com.atlashub.logistics.returns.infrastructure.persistence.entities.ReturnShipmentJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReturnShipmentRepository extends JpaRepository<ReturnShipmentJpa, Long> {

    Optional<ReturnShipmentJpa> findByReturnTrackingNumber(String trackingNumber);

    List<ReturnShipmentJpa> findByOrganizationId(Long organizationId);

    Optional<ReturnShipmentJpa> findByOriginalShipmentId(Long originalShipmentId);
}
