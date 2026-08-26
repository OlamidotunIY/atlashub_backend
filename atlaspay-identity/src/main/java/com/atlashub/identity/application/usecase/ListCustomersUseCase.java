package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.dto.CustomerDto;
import com.atlashub.identity.application.port.CustomerQueryService;
import com.atlashub.identity.application.query.ListCustomersQuery;
import com.atlashub.shared.usecase.BaseUseCase;
import com.atlashub.shared.util.PageResult;

@Service
public class ListCustomersUseCase extends BaseUseCase<ListCustomersQuery, PageResult<CustomerDto>> {
    private static final Logger log = LoggerFactory.getLogger(ListCustomersUseCase.class);


    private final CustomerQueryService queryService;

    public ListCustomersUseCase(CustomerQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public PageResult<CustomerDto> execute(ListCustomersQuery query) {
        return queryService.findAllByMerchantId(
                query.merchantId(),
                query.page(),
                query.size(),
                query.emailFilter()
        );
    }
}


