package com.atlashub.commerce.storefront.application.commands.CreateOnlineOrder;

import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.OrderItemDto;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class CreateOnlineOrderHandler extends Command<CreateOnlineOrderCommand, CreateOnlineOrderResult> {

    private final SalesOrderRepository salesOrderRepository;

    public CreateOnlineOrderHandler(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
    }

    @Override
    public CreateOnlineOrderResult execute(CreateOnlineOrderCommand command) {
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
                null,
                null,
                OrderType.ONLINE,
                command.paymentMethod(),
                orderItems,
                command.deliveryAddress()
        );

        SalesOrder savedOrder = salesOrderRepository.save(order);
        return new CreateOnlineOrderResult(savedOrder.getId());
    }
}
