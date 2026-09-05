package com.atlashub.identity.application.port;

import com.atlashub.identity.application.result.OrganizationProfileDto;
import com.atlashub.shared.application.util.PageResult;

import java.util.Optional;

public interface OrganizationQueryService {
    Optional<OrganizationProfileDto> getProfile(Long organizationId);
    PageResult<OrganizationProfileDto> findAll(int page, int size, String searchFilter);
    Optional<OrganizationSharedDto> getOrganizationById(Long organizationId);

    record OrganizationSharedDto(Long id, String businessName, com.atlashub.shared.domain.money.CurrencyCode currency) {}
}
