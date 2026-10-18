package com.atlashub.logistics.warehousing.infrastructure.persistence.repositories;

import com.atlashub.logistics.warehousing.infrastructure.persistence.entities.WarehouseInventoryJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWarehouseInventoryRepository extends JpaRepository<WarehouseInventoryJpa, Long> {

    Optional<WarehouseInventoryJpa> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    List<WarehouseInventoryJpa> findByWarehouseId(Long warehouseId);

    List<WarehouseInventoryJpa> findByOrganizationId(Long organizationId);
}
