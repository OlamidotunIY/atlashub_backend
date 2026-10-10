package com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ProcessPosCheckoutHandler extends Command<ProcessPosCheckoutCommand, ProcessPosCheckoutResult> {

    private final SalesOrderRepository salesOrderRepository;

    public ProcessPosCheckoutHandler(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
    }

    @Override
    public ProcessPosCheckoutResult execute(ProcessPosCheckoutCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        Long orderId = salesOrderRepository.nextIdentity();
        List<SalesOrderItem> orderItems = new ArrayList<>();
        if (command.items() != null) {
            for (OrderItemDto dto : command.items()) {
                orderItems.add(SalesOrderItem.create(
                        null,
                        orderId,
                        dto.productId(),
                        dto.variantId(),
                        dto.quantity(),
                        dto.unitPrice(),
                        dto.taxAmount(),
                        dto.discountAmount()
                ));
            }
        }

        SalesOrder order = SalesOrder.create(
                orderId,
                command.organizationId(),
                command.outletId(),
                null,
                command.customerId(),
                command.cashierId(),
                command.tillId(),
                command.type(),
                command.paymentMethod(),
                orderItems,
                null
        );

        SalesOrder savedOrder = salesOrderRepository.save(order);
        return new ProcessPosCheckoutResult(savedOrder.getId(), savedOrder.getStatus());
    }
}
