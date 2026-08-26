package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.dto.SubAccountDto;
import com.atlashub.identity.application.port.SubAccountQueryService;
import com.atlashub.identity.application.query.ListSubAccountsQuery;
import com.atlashub.shared.usecase.BaseUseCase;
import com.atlashub.shared.util.PageResult;

@Service
public class ListSubAccountsUseCase extends BaseUseCase<ListSubAccountsQuery, PageResult<SubAccountDto>> {
    private static final Logger log = LoggerFactory.getLogger(ListSubAccountsUseCase.class);


    private final SubAccountQueryService queryService;

    public ListSubAccountsUseCase(SubAccountQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public PageResult<SubAccountDto> execute(ListSubAccountsQuery query) {
        return queryService.findAllByMerchantId(
                query.merchantId(),
                query.page(),
                query.size()
        );
    }
}


