package com.atlashub.commerce.inventory.application.queries.GetStockSummary;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class GetStockSummaryHandler extends Query<GetStockSummaryQuery, StockSummaryResult> {

    private final InventoryRepository inventoryRepository;

    public GetStockSummaryHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
    }

    @Override
    public StockSummaryResult execute(GetStockSummaryQuery query) {
        List<Inventory> list =
                inventoryRepository.findByOrganizationIdAndOutletId(query.organizationId(), query.outletId());

        int totalSkus = list.size();
        int totalQuantity = 0;
        int lowStockCount = 0;
        int outOfStockCount = 0;

        for (Inventory inv : list) {
            totalQuantity += inv.getQuantity();
            if (inv.isLowStock()) {
                lowStockCount++;
            }
            if (inv.getQuantity() <= 0) {
                outOfStockCount++;
            }
        }

        return new StockSummaryResult(query.outletId(), totalSkus, totalQuantity, lowStockCount, outOfStockCount);
    }
}
