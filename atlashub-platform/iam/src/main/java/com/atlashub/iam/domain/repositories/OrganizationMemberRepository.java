package com.atlashub.iam.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends Repository<OrganizationMember> {
    List<OrganizationMember> findAllByOrganizationId(Long organizationId);
    List<OrganizationMember> findAllByOrganizationIdAndStatus(Long organizationId, MemberStatus status);
}
