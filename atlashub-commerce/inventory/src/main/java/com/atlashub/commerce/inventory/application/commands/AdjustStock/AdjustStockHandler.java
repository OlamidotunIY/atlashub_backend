package com.atlashub.commerce.inventory.application.commands.AdjustStock;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockAdjustment;
import com.atlashub.commerce.inventory.domain.exceptions.InventoryNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockAdjustmentRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AdjustStockHandler extends Command<AdjustStockCommand, Void> {

    private final InventoryRepository inventoryRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    public AdjustStockHandler(InventoryRepository inventoryRepository,
                              StockAdjustmentRepository stockAdjustmentRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
        this.stockAdjustmentRepository =
                Objects.requireNonNull(stockAdjustmentRepository, "StockAdjustmentRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:inventory:adjust')")
    public Void execute(AdjustStockCommand command) {
        Inventory inventory = inventoryRepository.findById(command.inventoryId())
                .orElseThrow(() -> new InventoryNotFoundException(command.inventoryId()));

        int previousQty = inventory.getQuantity();
        inventory.adjust(command.newQuantity(), command.reason(), command.adjustedBy());

        Long adjustmentId = stockAdjustmentRepository.nextIdentity();
        StockAdjustment adjustment =
                StockAdjustment.create(adjustmentId, inventory.getOrganizationId(), inventory.getId(),
                        command.adjustedBy(), previousQty, command.newQuantity(), command.reason());

        stockAdjustmentRepository.save(adjustment);
        inventoryRepository.save(inventory);
        return null;
    }
}
