package com.atlashub.identity.application.port;

import com.atlashub.identity.application.result.OrganizationMemberDto;
import com.atlashub.shared.application.util.PageResult;

import java.util.Optional;

public interface OrganizationMemberQueryService {
    Optional<OrganizationMemberDto> findById(Long organizationId, Long memberId);
    PageResult<OrganizationMemberDto> findAllByOrganizationId(Long organizationId, int page, int size, String searchFilter);
    boolean isUserMemberOfOrganization(Long userId, Long organizationId);
    String getOnboardingStatus(Long userId, String email);
}
