package com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn;

import com.atlashub.commerce.inventory.domain.entities.CustomerReturn;
import com.atlashub.commerce.inventory.domain.repositories.CustomerReturnRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreateCustomerReturnHandler extends Command<CreateCustomerReturnCommand, CreateCustomerReturnResult> {

    private final CustomerReturnRepository customerReturnRepository;

    public CreateCustomerReturnHandler(CustomerReturnRepository customerReturnRepository) {
        this.customerReturnRepository = Objects.requireNonNull(customerReturnRepository, "CustomerReturnRepository must not be null");
    }

    @Override
    public CreateCustomerReturnResult execute(CreateCustomerReturnCommand command) {
        Long returnId = customerReturnRepository.nextIdentity();
        CustomerReturn customerReturn = CustomerReturn.create(
                returnId,
                command.organizationId(),
                command.outletId(),
                command.salesOrderId(),
                command.customerId(),
                command.refundAmount(),
                command.reason(),
                command.refundMethod()
        );

        if (command.items() != null) {
            for (ReturnItemDto itemDto : command.items()) {
                Long itemId = customerReturnRepository.nextIdentity();
                customerReturn.addItem(itemId, itemDto.productId(), itemDto.quantity());
            }
        }

        customerReturnRepository.save(customerReturn);
        return new CreateCustomerReturnResult(returnId);
    }
}
