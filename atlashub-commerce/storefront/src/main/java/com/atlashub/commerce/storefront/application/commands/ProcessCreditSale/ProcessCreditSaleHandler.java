package com.atlashub.commerce.storefront.application.commands.ProcessCreditSale;

import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.OrderItemDto;
import com.atlashub.commerce.storefront.domain.entities.CustomerCredit;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.exceptions.CustomerCreditNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.CustomerCreditRepository;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ProcessCreditSaleHandler extends Command<ProcessCreditSaleCommand, ProcessCreditSaleResult> {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerCreditRepository customerCreditRepository;

    public ProcessCreditSaleHandler(
            SalesOrderRepository salesOrderRepository,
            CustomerCreditRepository customerCreditRepository
    ) {
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
        this.customerCreditRepository = Objects.requireNonNull(customerCreditRepository, "CustomerCreditRepository must not be null");
    }

    @Override
    public ProcessCreditSaleResult execute(ProcessCreditSaleCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        CustomerCredit credit = customerCreditRepository.findByOrganizationIdAndCustomerId(command.organizationId(), command.customerId())
                .orElseThrow(() -> new CustomerCreditNotFoundException(command.customerId()));

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
                null,
                OrderType.POS_RETAIL,
                PaymentMethod.CREDIT,
                orderItems,
                null
        );

        credit.extendCredit(order.getTotalNet());
        customerCreditRepository.save(credit);

        order.completePayment();
        SalesOrder savedOrder = salesOrderRepository.save(order);

        return new ProcessCreditSaleResult(savedOrder.getId());
    }
}
