package com.atlashub.logistics.warehousing.domain.entities;

import com.atlashub.logistics.warehousing.domain.events.WarehouseStockAdjustedEvent;
import com.atlashub.logistics.warehousing.domain.exceptions.InsufficientWarehouseStockException;
import com.atlashub.logistics.warehousing.domain.exceptions.WarehousingInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class WarehouseInventory extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long warehouseId;
    private final Long productId;
    private int quantity;
    private int reservedQuantity;
    private ZonedDateTime updatedAt;

    public WarehouseInventory(
            Long id,
            Long organizationId,
            Long warehouseId,
            Long productId,
            int quantity,
            int reservedQuantity,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.updatedAt = updatedAt;
    }

    public static WarehouseInventory create(
            Long id,
            Long organizationId,
            Long warehouseId,
            Long productId,
            int initialQuantity,
            String correlationId
    ) {
        if (id == null) {
            throw new WarehousingInvariantException("Inventory ID cannot be null");
        }
        if (organizationId == null) {
            throw new WarehousingInvariantException("Organization ID cannot be null");
        }
        if (warehouseId == null) {
            throw new WarehousingInvariantException("Warehouse ID cannot be null");
        }
        if (productId == null) {
            throw new WarehousingInvariantException("Product ID cannot be null");
        }
        if (initialQuantity < 0) {
            throw new WarehousingInvariantException("Initial quantity cannot be negative");
        }

        ZonedDateTime now = ZonedDateTime.now();
        WarehouseInventory inventory = new WarehouseInventory(
                id,
                organizationId,
                warehouseId,
                productId,
                initialQuantity,
                0,
                now
        );

        inventory.registerEvent(WarehouseStockAdjustedEvent.of(
                id,
                correlationId,
                new WarehouseStockAdjustedEvent.Payload(
                        id,
                        warehouseId,
                        productId,
                        initialQuantity,
                        0,
                        now
                )
        ));

        return inventory;
    }

    public void addStock(int qty, String correlationId) {
        if (qty <= 0) {
            throw new WarehousingInvariantException("Stock addition must be greater than zero");
        }
        this.quantity += qty;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(WarehouseStockAdjustedEvent.of(
                this.id,
                correlationId,
                new WarehouseStockAdjustedEvent.Payload(
                        this.id,
                        this.warehouseId,
                        this.productId,
                        this.quantity,
                        this.reservedQuantity,
                        this.updatedAt
                )
        ));
    }

    public void reserveStock(int qty, String correlationId) {
        if (qty <= 0) {
            throw new WarehousingInvariantException("Stock reservation quantity must be greater than zero");
        }
        int available = availableQuantity();
        if (available < qty) {
            throw new InsufficientWarehouseStockException(this.warehouseId, this.productId, qty, available);
        }
        this.reservedQuantity += qty;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(WarehouseStockAdjustedEvent.of(
                this.id,
                correlationId,
                new WarehouseStockAdjustedEvent.Payload(
                        this.id,
                        this.warehouseId,
                        this.productId,
                        this.quantity,
                        this.reservedQuantity,
                        this.updatedAt
                )
        ));
    }

    public void deductStock(int qty, String correlationId) {
        if (qty <= 0) {
            throw new WarehousingInvariantException("Deduction quantity must be greater than zero");
        }
        if (this.reservedQuantity < qty) {
            throw new WarehousingInvariantException(String.format(
                    "Cannot deduct %d items; only %d items are reserved", qty, this.reservedQuantity));
        }
        if (this.quantity < qty) {
            throw new WarehousingInvariantException(String.format(
                    "Cannot deduct %d items; total stock is only %d", qty, this.quantity));
        }
        this.reservedQuantity -= qty;
        this.quantity -= qty;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(WarehouseStockAdjustedEvent.of(
                this.id,
                correlationId,
                new WarehouseStockAdjustedEvent.Payload(
                        this.id,
                        this.warehouseId,
                        this.productId,
                        this.quantity,
                        this.reservedQuantity,
                        this.updatedAt
                )
        ));
    }

    public void releaseReservedStock(int qty, String correlationId) {
        if (qty <= 0) {
            throw new WarehousingInvariantException("Release quantity must be greater than zero");
        }
        if (this.reservedQuantity < qty) {
            throw new WarehousingInvariantException(String.format(
                    "Cannot release %d reserved items; only %d items are reserved", qty, this.reservedQuantity));
        }
        this.reservedQuantity -= qty;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(WarehouseStockAdjustedEvent.of(
                this.id,
                correlationId,
                new WarehouseStockAdjustedEvent.Payload(
                        this.id,
                        this.warehouseId,
                        this.productId,
                        this.quantity,
                        this.reservedQuantity,
                        this.updatedAt
                )
        ));
    }

    public void adjustStock(int newQuantity, String correlationId) {
        if (newQuantity < this.reservedQuantity) {
            throw new WarehousingInvariantException(String.format(
                    "New total quantity %d cannot be less than currently reserved quantity %d",
                    newQuantity, this.reservedQuantity));
        }
        this.quantity = newQuantity;
        this.updatedAt = ZonedDateTime.now();

        registerEvent(WarehouseStockAdjustedEvent.of(
                this.id,
                correlationId,
                new WarehouseStockAdjustedEvent.Payload(
                        this.id,
                        this.warehouseId,
                        this.productId,
                        this.quantity,
                        this.reservedQuantity,
                        this.updatedAt
                )
        ));
    }

    public int availableQuantity() {
        return this.quantity - this.reservedQuantity;
    }

    @Override
    public Long getId() {
        return id;
    }
}
