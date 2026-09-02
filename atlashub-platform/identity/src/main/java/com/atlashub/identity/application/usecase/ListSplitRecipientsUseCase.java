package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.result.SplitRecipientDto;
import com.atlashub.identity.application.port.SplitRecipientQueryService;
import com.atlashub.identity.application.query.ListSplitRecipientsQuery;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.application.util.PageResult;

@Service
public class ListSplitRecipientsUseCase extends BaseUseCase<ListSplitRecipientsQuery, PageResult<SplitRecipientDto>> {
    private static final Logger log = LoggerFactory.getLogger(ListSplitRecipientsUseCase.class);


    private final SplitRecipientQueryService queryService;

    public ListSplitRecipientsUseCase(SplitRecipientQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public PageResult<SplitRecipientDto> execute(ListSplitRecipientsQuery query) {
        return queryService.findAllByOrganizationId(
                query.OrganizationId(),
                query.page(),
                query.size()
        );
    }
}


