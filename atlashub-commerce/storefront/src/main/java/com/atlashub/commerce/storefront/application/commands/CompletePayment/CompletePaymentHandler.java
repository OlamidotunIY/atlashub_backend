package com.atlashub.commerce.storefront.application.commands.CompletePayment;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.exceptions.SalesOrderNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CompletePaymentHandler extends Command<CompletePaymentCommand, Void> {

    private final SalesOrderRepository salesOrderRepository;

    public CompletePaymentHandler(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
    }

    @Override
    public Void execute(CompletePaymentCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        SalesOrder order = salesOrderRepository.findById(command.salesOrderId())
                .orElseThrow(() -> new SalesOrderNotFoundException(command.salesOrderId()));

        order.completePayment();
        salesOrderRepository.save(order);
        return null;
    }
}
