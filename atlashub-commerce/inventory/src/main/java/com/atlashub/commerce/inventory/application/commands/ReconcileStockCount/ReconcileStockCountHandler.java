package com.atlashub.commerce.inventory.application.commands.ReconcileStockCount;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockAdjustment;
import com.atlashub.commerce.inventory.domain.entities.StockCount;
import com.atlashub.commerce.inventory.domain.entities.StockCountItem;
import com.atlashub.commerce.inventory.domain.exceptions.InventoryNotFoundException;
import com.atlashub.commerce.inventory.domain.exceptions.StockCountNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockAdjustmentRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockCountRepository;
import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ReconcileStockCountHandler extends Command<ReconcileStockCountCommand, Void> {

    private final StockCountRepository stockCountRepository;
    private final InventoryRepository inventoryRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    public ReconcileStockCountHandler(StockCountRepository stockCountRepository,
                                      InventoryRepository inventoryRepository,
                                      StockAdjustmentRepository stockAdjustmentRepository) {
        this.stockCountRepository = Objects.requireNonNull(stockCountRepository, "StockCountRepository must not be null");
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
        this.stockAdjustmentRepository = Objects.requireNonNull(stockAdjustmentRepository, "StockAdjustmentRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:inventory:adjust')")
    public Void execute(ReconcileStockCountCommand command) {
        StockCount stockCount = stockCountRepository.findById(command.stockCountId())
                .orElseThrow(() -> new StockCountNotFoundException(command.stockCountId()));

        if (command.countedItems() != null) {
            for (CountedItemDto item : command.countedItems()) {
                stockCount.updateCount(item.inventoryId(), item.countedQty());
            }
        }

        stockCount.reconcile();

        for (StockCountItem item : stockCount.getItems()) {
            if (item.getCountedQty() != null && !item.getCountedQty().equals(item.getSystemQty())) {
                Inventory inventory = inventoryRepository.findById(item.getInventoryId())
                        .orElseThrow(() -> new InventoryNotFoundException(item.getInventoryId()));

                int previousQty = inventory.getQuantity();
                inventory.adjust(item.getCountedQty(), AdjustmentReason.CORRECTION, command.reconciledBy());
                inventoryRepository.save(inventory);

                Long adjId = stockAdjustmentRepository.nextIdentity();
                StockAdjustment adjustment = StockAdjustment.create(
                        adjId,
                        stockCount.getOrganizationId(),
                        item.getInventoryId(),
                        command.reconciledBy(),
                        previousQty,
                        item.getCountedQty(),
                        AdjustmentReason.CORRECTION
                );
                stockAdjustmentRepository.save(adjustment);
            }
        }

        stockCountRepository.save(stockCount);
        return null;
    }
}
