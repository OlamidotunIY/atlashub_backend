package com.atlashub.logistics.shipping.infrastructure.persistence.repositories;

import com.atlashub.logistics.shipping.infrastructure.persistence.entities.DispatchRiderJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataDispatchRiderRepository extends JpaRepository<DispatchRiderJpa, Long> {

    Optional<DispatchRiderJpa> findByUserId(Long userId);

    List<DispatchRiderJpa> findByOrganizationId(Long organizationId);

    @Query("SELECT r FROM DispatchRiderJpa r WHERE r.zoneCode = :zone AND r.status = 'AVAILABLE'")
    List<DispatchRiderJpa> findAvailableRidersByZone(@Param("zone") String zoneCode);
}
