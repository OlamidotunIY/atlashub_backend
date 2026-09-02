package com.atlashub.shared.application.api;

import java.util.Optional;

public interface OrganizationQueryApi {
    Optional<OrganizationSharedDto> getOrganizationById(Long organizationId);

    record OrganizationSharedDto(
        Long id,
        String businessName
    ) {}
}
