package com.atlashub.commerce.storefront.application.commands.HandleStockReserved;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.exceptions.SalesOrderNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class HandleStockReservedHandler extends Command<HandleStockReservedCommand, Void> {

    private final SalesOrderRepository salesOrderRepository;

    public HandleStockReservedHandler(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
    }

    @Override
    public Void execute(HandleStockReservedCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        SalesOrder order = salesOrderRepository.findById(command.salesOrderId())
                .orElseThrow(() -> new SalesOrderNotFoundException(command.salesOrderId()));

        if (order.getPaymentMethod() == PaymentMethod.CASH) {
            order.completePayment();
        } else if (order.getPaymentMethod() != PaymentMethod.CREDIT) {
            String chargeRef = order.getChargeReference() != null
                    ? order.getChargeReference()
                    : UUID.randomUUID().toString();
            order.initiatePayment(chargeRef);
        }

        salesOrderRepository.save(order);
        return null;
    }
}
