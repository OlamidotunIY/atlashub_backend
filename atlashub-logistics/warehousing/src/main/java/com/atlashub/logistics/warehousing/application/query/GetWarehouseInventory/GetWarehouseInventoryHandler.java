package com.atlashub.logistics.warehousing.application.query.GetWarehouseInventory;

import com.atlashub.logistics.warehousing.domain.repositories.WarehouseInventoryRepository;
import com.atlashub.shared.application.usecase.Query;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetWarehouseInventoryHandler extends Query<GetWarehouseInventoryQuery, List<WarehouseInventoryResult>> {

    private final WarehouseInventoryRepository inventoryRepository;

    public GetWarehouseInventoryHandler(WarehouseInventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseInventoryResult> execute(GetWarehouseInventoryQuery query) {
        return inventoryRepository.findByWarehouseId(query.warehouseId())
                .stream()
                .map(inv -> new WarehouseInventoryResult(
                        inv.getId(),
                        inv.getOrganizationId(),
                        inv.getWarehouseId(),
                        inv.getProductId(),
                        inv.getQuantity(),
                        inv.getReservedQuantity(),
                        inv.availableQuantity(),
                        inv.getUpdatedAt()
                ))
                .toList();
    }
}
