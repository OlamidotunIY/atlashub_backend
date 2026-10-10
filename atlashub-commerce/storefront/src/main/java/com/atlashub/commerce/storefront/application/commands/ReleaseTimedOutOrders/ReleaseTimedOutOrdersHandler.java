package com.atlashub.commerce.storefront.application.commands.ReleaseTimedOutOrders;

import com.atlashub.commerce.storefront.application.commands.FailPayment.FailPaymentCommand;
import com.atlashub.commerce.storefront.application.commands.FailPayment.FailPaymentHandler;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class ReleaseTimedOutOrdersHandler extends Command<ReleaseTimedOutOrdersCommand, Void> {

    private final SalesOrderRepository salesOrderRepository;
    private final FailPaymentHandler failPaymentHandler;

    public ReleaseTimedOutOrdersHandler(
            SalesOrderRepository salesOrderRepository,
            FailPaymentHandler failPaymentHandler
    ) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
        this.failPaymentHandler = Objects.requireNonNull(failPaymentHandler, "FailPaymentHandler must not be null");
    }

    @Override
    public Void execute(ReleaseTimedOutOrdersCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(command.timeoutMinutes());
        List<SalesOrder> timedOutOrders = salesOrderRepository.findByStatusAndSaleDateBefore(
                OrderStatus.PAYMENT_PENDING,
                cutoff
        );

        for (SalesOrder order : timedOutOrders) {
            failPaymentHandler.execute(new FailPaymentCommand(order.getId(), "Payment timed out"));
        }

        return null;
    }
}
