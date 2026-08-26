package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.dto.CustomerDto;
import com.atlashub.identity.application.port.CustomerQueryService;
import com.atlashub.identity.application.query.GetCustomerQuery;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;

@Service
public class GetCustomerUseCase extends BaseUseCase<GetCustomerQuery, CustomerDto> {
    private static final Logger log = LoggerFactory.getLogger(GetCustomerUseCase.class);


    private final CustomerQueryService queryService;

    public GetCustomerUseCase(CustomerQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public CustomerDto execute(GetCustomerQuery query) {
        log.info("Executing GetCustomerUseCase");

        return queryService.findById(query.merchantId(), query.customerId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));
    }
}


