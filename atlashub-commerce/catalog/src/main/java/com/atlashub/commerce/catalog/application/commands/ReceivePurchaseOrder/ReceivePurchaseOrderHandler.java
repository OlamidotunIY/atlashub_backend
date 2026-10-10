package com.atlashub.commerce.catalog.application.commands.ReceivePurchaseOrder;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrder;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidPurchaseOrderStateException;
import com.atlashub.commerce.catalog.domain.exceptions.PurchaseOrderNotFoundException;
import com.atlashub.commerce.catalog.domain.repositories.PurchaseOrderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ReceivePurchaseOrderHandler extends Command<ReceivePurchaseOrderCommand, Void> {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public ReceivePurchaseOrderHandler(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = Objects.requireNonNull(purchaseOrderRepository, "PurchaseOrderRepository must not be null");
    }

    @Override
    public Void execute(ReceivePurchaseOrderCommand command) {
        PurchaseOrder po = purchaseOrderRepository.findById(command.purchaseOrderId())
                .orElseThrow(() -> new PurchaseOrderNotFoundException(command.purchaseOrderId()));

        if (command.receivedItems() == null || command.receivedItems().isEmpty()) {
            throw new InvalidPurchaseOrderStateException("Received items cannot be null or empty");
        }

        Map<Long, Integer> receivedMap = new HashMap<>();
        for (ReceivedItemDto item : command.receivedItems()) {
            receivedMap.put(item.productId(), item.quantityReceived());
        }

        po.receiveItems(receivedMap);
        purchaseOrderRepository.save(po);
        return null;
    }
}
