package com.atlashub.commerce.catalog.application.commands.SendPurchaseOrder;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrder;
import com.atlashub.commerce.catalog.domain.exceptions.PurchaseOrderNotFoundException;
import com.atlashub.commerce.catalog.domain.repositories.PurchaseOrderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SendPurchaseOrderHandler extends Command<SendPurchaseOrderCommand, Void> {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public SendPurchaseOrderHandler(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = Objects.requireNonNull(purchaseOrderRepository, "PurchaseOrderRepository must not be null");
    }

    @Override
    public Void execute(SendPurchaseOrderCommand command) {
        PurchaseOrder po = purchaseOrderRepository.findById(command.purchaseOrderId())
                .orElseThrow(() -> new PurchaseOrderNotFoundException(command.purchaseOrderId()));

        po.send();
        purchaseOrderRepository.save(po);
        return null;
    }
}
