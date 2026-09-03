package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.result.SplitRecipientDto;
import com.atlashub.identity.application.port.SplitRecipientQueryService;
import com.atlashub.identity.application.query.GetSplitRecipientQuery;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;

@Service
public class GetSplitRecipientUseCase extends BaseUseCase<GetSplitRecipientQuery, SplitRecipientDto> {
    private static final Logger log = LoggerFactory.getLogger(GetSplitRecipientUseCase.class);


    private final SplitRecipientQueryService queryService;

    public GetSplitRecipientUseCase(SplitRecipientQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public SplitRecipientDto execute(GetSplitRecipientQuery query) {
        log.info("Executing GetSplitRecipientUseCase");

        return queryService.findById(query.OrganizationId(), query.SplitRecipientId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.SplitRecipient_NOT_FOUND, "SplitRecipient not found"));
    }
}


