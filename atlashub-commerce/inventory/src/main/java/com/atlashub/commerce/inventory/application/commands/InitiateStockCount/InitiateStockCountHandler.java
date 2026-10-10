package com.atlashub.commerce.inventory.application.commands.InitiateStockCount;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockCount;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockCountRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class InitiateStockCountHandler extends Command<InitiateStockCountCommand, InitiateStockCountResult> {

    private final StockCountRepository stockCountRepository;
    private final InventoryRepository inventoryRepository;

    public InitiateStockCountHandler(StockCountRepository stockCountRepository,
                                     InventoryRepository inventoryRepository) {
        this.stockCountRepository = Objects.requireNonNull(stockCountRepository, "StockCountRepository must not be null");
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
    }

    @Override
    @PreAuthorize("hasAuthority('commerce:inventory:adjust')")
    public InitiateStockCountResult execute(InitiateStockCountCommand command) {
        Long stockCountId = stockCountRepository.nextIdentity();
        StockCount stockCount = StockCount.create(stockCountId, command.organizationId(), command.outletId());

        List<Inventory> inventories = inventoryRepository.findByOrganizationIdAndOutletId(
                command.organizationId(), command.outletId()
        );

        for (Inventory inventory : inventories) {
            Long itemId = stockCountRepository.nextIdentity();
            stockCount.addItem(itemId, inventory.getId(), inventory.getProductId(), inventory.getQuantity());
        }

        stockCountRepository.save(stockCount);
        return new InitiateStockCountResult(stockCountId);
    }
}
