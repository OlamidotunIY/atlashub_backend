package com.atlashub.commerce.storefront.application.commands.AddCustomerDepositPayment;

import com.atlashub.commerce.storefront.domain.entities.CustomerDeposit;
import com.atlashub.commerce.storefront.domain.exceptions.CustomerDepositNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.CustomerDepositRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AddCustomerDepositPaymentHandler extends Command<AddCustomerDepositPaymentCommand, Void> {

    private final CustomerDepositRepository depositRepository;

    public AddCustomerDepositPaymentHandler(CustomerDepositRepository depositRepository) {
        this.depositRepository = Objects.requireNonNull(depositRepository, "CustomerDepositRepository must not be null");
    }

    @Override
    public Void execute(AddCustomerDepositPaymentCommand command) {
        Objects.requireNonNull(command, "Command must not be null");

        CustomerDeposit deposit = depositRepository.findById(command.depositId())
                .orElseThrow(() -> new CustomerDepositNotFoundException(command.depositId()));

        deposit.addPayment(command.paymentAmount());
        depositRepository.save(deposit);
        return null;
    }
}
