package com.atlashub.logistics.warehousing.application.command.AdjustWarehouseStock;

import com.atlashub.logistics.warehousing.domain.entities.Warehouse;
import com.atlashub.logistics.warehousing.domain.entities.WarehouseInventory;
import com.atlashub.logistics.warehousing.domain.exceptions.WarehouseInactiveException;
import com.atlashub.logistics.warehousing.domain.exceptions.WarehouseNotFoundException;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseInventoryRepository;
import com.atlashub.logistics.warehousing.domain.repositories.WarehouseRepository;
import com.atlashub.logistics.warehousing.domain.valueobject.WarehouseStatus;
import com.atlashub.shared.application.usecase.Command;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdjustWarehouseStockHandler extends Command<AdjustWarehouseStockCommand, AdjustWarehouseStockResult> {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseInventoryRepository inventoryRepository;

    public AdjustWarehouseStockHandler(
            WarehouseRepository warehouseRepository,
            WarehouseInventoryRepository inventoryRepository
    ) {
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public AdjustWarehouseStockResult execute(AdjustWarehouseStockCommand input) {
        Warehouse warehouse = warehouseRepository.findById(input.warehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(input.warehouseId()));

        if (warehouse.getStatus() == WarehouseStatus.INACTIVE) {
            throw new WarehouseInactiveException(input.warehouseId());
        }

        Optional<WarehouseInventory> existingOpt = inventoryRepository.findByWarehouseIdAndProductId(
                input.warehouseId(), input.productId());

        WarehouseInventory inventory;
        if (existingOpt.isPresent()) {
            inventory = existingOpt.get();
            inventory.adjustStock(input.newQuantity(), input.correlationId());
        } else {
            Long id = inventoryRepository.nextIdentity();
            inventory = WarehouseInventory.create(
                    id,
                    input.organizationId(),
                    input.warehouseId(),
                    input.productId(),
                    input.newQuantity(),
                    input.correlationId()
            );
        }

        inventoryRepository.save(inventory);

        return new AdjustWarehouseStockResult(
                inventory.getId(),
                inventory.getWarehouseId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.availableQuantity()
        );
    }
}
