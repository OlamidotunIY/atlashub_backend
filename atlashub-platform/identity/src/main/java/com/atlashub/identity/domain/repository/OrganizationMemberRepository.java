package com.atlashub.identity.domain.repository;

import com.atlashub.identity.domain.model.OrganizationMember;
import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository {
    OrganizationMember save(OrganizationMember member);
    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    List<OrganizationMember> findByOrganizationId(Long organizationId);
    List<OrganizationMember> findByUserId(Long userId);
}
