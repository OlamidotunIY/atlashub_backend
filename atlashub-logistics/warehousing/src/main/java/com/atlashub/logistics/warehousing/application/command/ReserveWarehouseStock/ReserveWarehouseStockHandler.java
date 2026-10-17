package com.atlashub.logistics.warehousing.application.command.ReserveWarehouseStock;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.logistics.warehousing.domain.entities.WarehouseInventory;
import com.atlashub.logistics.warehousing.domain.exceptions.InsufficientWarehouseStockException;
import com.atlashub.logistics.warehousing.domain.exceptions.WarehouseInactiveException;
import com.atlashub.logistics.warehousing.domain.exceptions.WarehouseNotFoundException;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseInventoryRepository;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseRepository;
import com.atlashub.logistics.warehousing.domain.valueobject.WarehouseStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReserveWarehouseStockHandler extends Command<ReserveWarehouseStockCommand, ReserveWarehouseStockResult> {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseInventoryRepository inventoryRepository;

    public ReserveWarehouseStockHandler(
            WarehouseRepository warehouseRepository,
            WarehouseInventoryRepository inventoryRepository
    ) {
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public ReserveWarehouseStockResult execute(ReserveWarehouseStockCommand input) {
        Warehouse warehouse = warehouseRepository.findById(input.warehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(input.warehouseId()));

        if (warehouse.getStatus() == WarehouseStatus.INACTIVE) {
            throw new WarehouseInactiveException(input.warehouseId());
        }

        WarehouseInventory inventory = inventoryRepository.findByWarehouseIdAndProductId(input.warehouseId(), input.productId())
                .orElseThrow(() -> new InsufficientWarehouseStockException(input.warehouseId(), input.productId(), input.quantity(), 0));

        inventory.reserveStock(input.quantity(), input.correlationId());
        inventoryRepository.save(inventory);

        return new ReserveWarehouseStockResult(
                inventory.getId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.availableQuantity()
        );
    }
}
