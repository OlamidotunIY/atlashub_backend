package com.atlashub.logistics.warehousing.application.command.ReleaseWarehouseStock;

import com.atlashub.logistics.warehousing.domain.entities.WarehouseInventory;
import com.atlashub.logistics.warehousing.domain.exceptions.InsufficientWarehouseStockException;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseInventoryRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReleaseWarehouseStockHandler extends Command<ReleaseWarehouseStockCommand, ReleaseWarehouseStockResult> {

    private final WarehouseInventoryRepository inventoryRepository;

    public ReleaseWarehouseStockHandler(WarehouseInventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public ReleaseWarehouseStockResult execute(ReleaseWarehouseStockCommand input) {
        WarehouseInventory inventory = inventoryRepository.findByWarehouseIdAndProductId(input.warehouseId(), input.productId())
                .orElseThrow(() -> new InsufficientWarehouseStockException(input.warehouseId(), input.productId(), input.quantity(), 0));

        inventory.releaseReservedStock(input.quantity(), input.correlationId());
        inventoryRepository.save(inventory);

        return new ReleaseWarehouseStockResult(
                inventory.getId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.availableQuantity()
        );
    }
}
