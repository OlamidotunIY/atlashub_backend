package com.atlashub.commerce.inventory.application.queries.ListLowStockProducts;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ListLowStockProductsHandler extends Query<ListLowStockProductsQuery, List<LowStockResult>> {

    private final InventoryRepository inventoryRepository;

    public ListLowStockProductsHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
    }

    @Override
    public List<LowStockResult> execute(ListLowStockProductsQuery query) {
        List<Inventory> lowStock = inventoryRepository.findLowStock(query.organizationId(), query.outletId());

        return lowStock.stream()
                .map(this::toResult)
                .toList();
    }

    private LowStockResult toResult(Inventory inventory) {
        return new LowStockResult(
                inventory.getId(),
                inventory.getOrganizationId(),
                inventory.getOutletId(),
                inventory.getProductId(),
                inventory.getVariantId(),
                inventory.getQuantity(),
                inventory.getReorderLevel(),
                inventory.getSafeStock()
        );
    }
}
