package com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder;

import com.atlashub.commerce.catalog.domain.entities.PurchaseOrder;
import com.atlashub.commerce.catalog.domain.exceptions.SupplierNotFoundException;
import com.atlashub.commerce.catalog.domain.repositories.PurchaseOrderRepository;
import com.atlashub.commerce.catalog.domain.repositories.SupplierRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreatePurchaseOrderHandler extends Command<CreatePurchaseOrderCommand, CreatePurchaseOrderResult> {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;

    public CreatePurchaseOrderHandler(PurchaseOrderRepository purchaseOrderRepository,
                                      SupplierRepository supplierRepository) {
        this.purchaseOrderRepository = Objects.requireNonNull(purchaseOrderRepository, "PurchaseOrderRepository must not be null");
        this.supplierRepository = Objects.requireNonNull(supplierRepository, "SupplierRepository must not be null");
    }

    @Override
    public CreatePurchaseOrderResult execute(CreatePurchaseOrderCommand command) {
        supplierRepository.findById(command.supplierId())
                .orElseThrow(() -> new SupplierNotFoundException(command.supplierId()));

        Long poId = purchaseOrderRepository.nextIdentity();
        PurchaseOrder po = PurchaseOrder.create(
                poId,
                command.organizationId(),
                command.outletId(),
                command.supplierId(),
                command.expectedDeliveryDate(),
                command.currency()
        );

        if (command.items() != null) {
            for (PurchaseOrderItemDto itemDto : command.items()) {
                Long itemId = purchaseOrderRepository.nextIdentity();
                po.addItem(itemId, itemDto.productId(), itemDto.quantity(), itemDto.unitCost());
            }
        }

        purchaseOrderRepository.save(po);
        return new CreatePurchaseOrderResult(poId);
    }
}
