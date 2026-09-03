package com.atlashub.identity.application.usecase;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlashub.identity.application.result.OrganizationProfileDto;
import com.atlashub.identity.application.port.OrganizationQueryService;
import com.atlashub.identity.application.query.GetOrganizationProfileQuery;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;

@Service
public class GetOrganizationProfileUseCase extends BaseUseCase<GetOrganizationProfileQuery, OrganizationProfileDto> {
    private static final Logger log = LoggerFactory.getLogger(GetOrganizationProfileUseCase.class);


    private final OrganizationQueryService queryService;

    public GetOrganizationProfileUseCase(OrganizationQueryService queryService) {
        this.queryService = queryService;
    }

    @Override
    public OrganizationProfileDto execute(GetOrganizationProfileQuery query) {
        log.info("Executing GetOrganizationProfileUseCase");

        return queryService.findProfileById(query.OrganizationId())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.Organization_NOT_FOUND, "Organization not found"));
    }
}


