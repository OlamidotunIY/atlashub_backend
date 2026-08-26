package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.dto.ApiKeyDto;
import com.atlashub.identity.application.port.ApiKeyQueryService;
import com.atlashub.identity.application.query.ListApiKeysQuery;
import com.atlashub.shared.usecase.BaseUseCase;

import java.util.List;

@Service
public class ListApiKeysUseCase extends BaseUseCase<ListApiKeysQuery, List<ApiKeyDto>> {
    private static final Logger log = LoggerFactory.getLogger(ListApiKeysUseCase.class);


    private final ApiKeyQueryService queryService;

    public ListApiKeysUseCase(ApiKeyQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public List<ApiKeyDto> execute(ListApiKeysQuery query) {
        return queryService.findAllByIntegration(query.OrganizationId());
    }
}


