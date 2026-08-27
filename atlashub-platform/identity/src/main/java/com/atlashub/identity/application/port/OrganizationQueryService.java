package com.atlashub.identity.application.port;

import com.atlashub.identity.application.result.OrganizationProfileDto;

import java.util.Optional;

public interface OrganizationQueryService {
    Optional<OrganizationProfileDto> findProfileById(Long OrganizationId);
}
