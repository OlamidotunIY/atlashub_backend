package com.atlashub.logistics.shipping.domain.repositories;

import com.atlashub.logistics.shipping.domain.entities.DispatchRider;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface DispatchRiderRepository extends Repository<DispatchRider> {

    Optional<DispatchRider> findByUserId(Long userId);

    List<DispatchRider> findByOrganizationId(Long organizationId);

    List<DispatchRider> findAvailableRidersByZone(String zoneCode);
}
