package com.atlashub.commerce.storefront.application.queries.GetCustomerCredit;

import com.atlashub.commerce.storefront.domain.entities.CustomerCredit;
import com.atlashub.commerce.storefront.domain.exceptions.CustomerCreditNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.CustomerCreditRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GetCustomerCreditHandler extends Query<GetCustomerCreditQuery, CustomerCreditResult> {

    private final CustomerCreditRepository customerCreditRepository;

    public GetCustomerCreditHandler(CustomerCreditRepository customerCreditRepository) {
        this.customerCreditRepository = Objects.requireNonNull(customerCreditRepository, "CustomerCreditRepository must not be null");
    }

    @Override
    public CustomerCreditResult execute(GetCustomerCreditQuery query) {
        Objects.requireNonNull(query, "Query must not be null");

        CustomerCredit credit = customerCreditRepository.findByOrganizationIdAndCustomerId(
                query.organizationId(),
                query.customerId()
        ).orElseThrow(() -> new CustomerCreditNotFoundException(query.customerId()));

        return new CustomerCreditResult(
                credit.getId(),
                credit.getOrganizationId(),
                credit.getCustomerId(),
                credit.getCreditLimit(),
                credit.getOutstandingDebt(),
                credit.getStatus()
        );
    }
}
