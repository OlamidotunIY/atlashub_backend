package com.atlashub.commerce.storefront.application.commands.RefundPosSale;

import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.exceptions.SalesOrderNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RefundPosSaleHandler extends Command<RefundPosSaleCommand, Void> {

    private final SalesOrderRepository salesOrderRepository;

    public RefundPosSaleHandler(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
    }

    @Override
    public Void execute(RefundPosSaleCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        SalesOrder order = salesOrderRepository.findById(command.salesOrderId())
                .orElseThrow(() -> new SalesOrderNotFoundException(command.salesOrderId()));

        order.refund(command.reason(), command.customerNuban(), command.customerBankCode());
        salesOrderRepository.save(order);
        return null;
    }
}
