package com.atlashub.logistics.warehousing.infrastructure.persistence.repositories;

import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.WarehouseJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWarehouseRepository extends JpaRepository<WarehouseJpa, Long> {

    Optional<WarehouseJpa> findByCode(String code);

    Optional<WarehouseJpa> findByOrganizationIdAndCode(Long organizationId, String code);

    List<WarehouseJpa> findByOrganizationId(Long organizationId);
}
