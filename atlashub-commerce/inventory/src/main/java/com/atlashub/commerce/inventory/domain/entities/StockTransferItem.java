package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.exceptions.InvalidTransferStateException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class StockTransferItem {

    private final Long id;
    private final Long transferId;
    private final Long productId;
    private final Integer quantityRequested;
    private Integer quantityReceived;

    public StockTransferItem(Long id, Long transferId, Long productId,
                             Integer quantityRequested, Integer quantityReceived) {
        this.id = id;
        this.transferId = transferId;
        this.productId = productId;
        this.quantityRequested = quantityRequested;
        this.quantityReceived = quantityReceived;
    }

    public static StockTransferItem create(Long id, Long transferId, Long productId, int quantityRequested) {
        Objects.requireNonNull(id, "Item ID must not be null");
        Objects.requireNonNull(transferId, "Transfer ID must not be null");
        Objects.requireNonNull(productId, "Product ID must not be null");
        if (quantityRequested <= 0) {
            throw new InvalidTransferStateException("Requested quantity must be positive");
        }

        return new StockTransferItem(id, transferId, productId, quantityRequested, null);
    }

    public void recordReceived(int received) {
        if (received < 0) {
            throw new InvalidTransferStateException("Received quantity cannot be negative");
        }
        this.quantityReceived = received;
    }

    public Long getId() {
        return id;
    }
}
