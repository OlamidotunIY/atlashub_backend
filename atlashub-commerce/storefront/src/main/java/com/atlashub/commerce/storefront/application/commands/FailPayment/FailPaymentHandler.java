package com.atlashub.commerce.storefront.application.commands.FailPayment;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.exceptions.SalesOrderNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FailPaymentHandler extends Command<FailPaymentCommand, Void> {

    private final SalesOrderRepository salesOrderRepository;

    public FailPaymentHandler(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
    }

    @Override
    public Void execute(FailPaymentCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        SalesOrder order = salesOrderRepository.findById(command.salesOrderId())
                .orElseThrow(() -> new SalesOrderNotFoundException(command.salesOrderId()));

        order.failPayment(command.reason());
        salesOrderRepository.save(order);
        return null;
    }
}
