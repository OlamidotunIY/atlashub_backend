package com.atlashub.logistics.warehousing.domain.repositories;

import com.atlashub.logistics.warehousing.domain.entities.WarehouseInventory;
import com.atlashub.shared.domain.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface WarehouseInventoryRepository extends Repository<WarehouseInventory> {

    Optional<WarehouseInventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    List<WarehouseInventory> findByWarehouseId(Long warehouseId);

    List<WarehouseInventory> findByOrganizationId(Long organizationId);
}
