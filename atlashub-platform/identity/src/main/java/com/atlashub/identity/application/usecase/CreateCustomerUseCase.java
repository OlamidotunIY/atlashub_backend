package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.command.CreateCustomerCommand;

import com.atlashub.shared.usecase.BaseUseCase;

import com.atlashub.identity.application.dto.CreateCustomerResult;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Customer;
import com.atlashub.identity.domain.repository.CustomerRepository;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.ConflictException;

@Service
public class CreateCustomerUseCase extends BaseUseCase<CreateCustomerCommand, CreateCustomerResult> {
    private static final Logger log = LoggerFactory.getLogger(CreateCustomerUseCase.class);


    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateCustomerUseCase(CustomerRepository customerRepository, DomainEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public CreateCustomerResult execute(CreateCustomerCommand command) {
        log.info("Executing CreateCustomerUseCase");

        if (customerRepository.findByMerchantIdAndEmail(command.merchantId(), command.email()).isPresent()) {
            throw new ConflictException(IdentityErrorCode.CUSTOMER_EMAIL_ALREADY_EXISTS, "Customer with this email already exists for this merchant");
        }

        Customer customer = new Customer(customerRepository.nextIdentity(),
                "CUS_" + java.util.UUID.randomUUID().toString(),
                command.merchantId(),
            command.firstName(),
            command.lastName(),
            new EmailAddress(command.email()),
            new PhoneNumber(command.phone()),
            command.metadata()
        );

        customerRepository.save(customer);

        publishEvents(customer, eventPublisher);

        return new CreateCustomerResult(customer.getId());
    }
}



