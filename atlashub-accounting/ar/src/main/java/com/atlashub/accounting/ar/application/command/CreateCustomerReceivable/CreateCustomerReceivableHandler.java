package com.atlashub.accounting.ar.application.command.CreateCustomerReceivable;

import com.atlashub.accounting.ar.domain.entities.CustomerReceivable;
import com.atlashub.accounting.ar.domain.repositories.CustomerReceivableRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CreateCustomerReceivableHandler extends Command<CreateCustomerReceivableCommand, Long> {

    private final CustomerReceivableRepository receivableRepository;

    public CreateCustomerReceivableHandler(CustomerReceivableRepository receivableRepository) {
        this.receivableRepository = receivableRepository;
    }

    @Override
    public Long execute(CreateCustomerReceivableCommand command) {
        Optional<CustomerReceivable> existing = receivableRepository.findByOrganizationIdAndSalesOrderId(
                command.organizationId(),
                command.salesOrderId()
        );
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        Long receivableId = receivableRepository.nextIdentity();
        CustomerReceivable receivable = CustomerReceivable.create(
                receivableId,
                command.organizationId(),
                command.customerId(),
                command.salesOrderId(),
                command.amount(),
                command.dueDate()
        );

        CustomerReceivable saved = receivableRepository.save(receivable);
        return saved.getId();
    }
}
