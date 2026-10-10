package com.atlashub.commerce.storefront.application.commands.CreateCustomerDeposit;

import com.atlashub.commerce.storefront.domain.entities.CustomerDeposit;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.exceptions.SalesOrderNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.CustomerDepositRepository;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CreateCustomerDepositHandler extends Command<CreateCustomerDepositCommand, CreateCustomerDepositResult> {

    private final CustomerDepositRepository depositRepository;
    private final SalesOrderRepository salesOrderRepository;

    public CreateCustomerDepositHandler(
            CustomerDepositRepository depositRepository,
            SalesOrderRepository salesOrderRepository
    ) {
        this.depositRepository = Objects.requireNonNull(depositRepository, "CustomerDepositRepository must not be null");
        this.salesOrderRepository = Objects.requireNonNull(salesOrderRepository, "SalesOrderRepository must not be null");
    }

    @Override
    public CreateCustomerDepositResult execute(CreateCustomerDepositCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        SalesOrder order = salesOrderRepository.findById(command.salesOrderId())
                .orElseThrow(() -> new SalesOrderNotFoundException(command.salesOrderId()));

        Long depositId = depositRepository.nextIdentity();
        CustomerDeposit deposit = CustomerDeposit.create(
                depositId,
                command.salesOrderId(),
                command.initialDeposit(),
                command.totalAmount()
        );

        order.convertToLayaway(depositId, command.initialDeposit());
        salesOrderRepository.save(order);

        CustomerDeposit savedDeposit = depositRepository.save(deposit);
        return new CreateCustomerDepositResult(savedDeposit.getId());
    }
}
