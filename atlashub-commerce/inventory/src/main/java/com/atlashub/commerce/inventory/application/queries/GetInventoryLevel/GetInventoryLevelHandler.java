package com.atlashub.commerce.inventory.application.queries.GetInventoryLevel;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.exceptions.InventoryNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class GetInventoryLevelHandler extends Query<GetInventoryLevelQuery, InventoryResult> {

    private final InventoryRepository inventoryRepository;

    public GetInventoryLevelHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository must not be null");
    }

    @Override
    public InventoryResult execute(GetInventoryLevelQuery query) {
        Optional<Inventory> inventoryOpt;
        if (query.variantId() != null) {
            inventoryOpt =
                    inventoryRepository.findByOrganizationIdAndOutletIdAndProductIdAndVariantId(query.organizationId(),
                            query.outletId(), query.productId(), query.variantId());
        } else {
            inventoryOpt = inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(query.organizationId(),
                    query.outletId(), query.productId());
        }

        Inventory inventory =
                inventoryOpt.orElseThrow(() -> new InventoryNotFoundException(query.productId(), query.outletId()));
        int available = inventory.getQuantity() - inventory.getReservedQuantity();

        return new InventoryResult(inventory.getId(), inventory.getOrganizationId(), inventory.getOutletId(),
                inventory.getProductId(), inventory.getVariantId(), inventory.getQuantity(),
                inventory.getReservedQuantity(), available, inventory.getReorderLevel(), inventory.getSafeStock(),
                inventory.isLowStock());
    }
}
