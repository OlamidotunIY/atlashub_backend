package com.atlashub.iam.infrastructure.persistence.repositories;

import com.atlashub.iam.domain.valueobject.MemberStatus;
import com.atlashub.iam.infrastructure.persistence.entities.OrganizationMemberJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataOrganizationMemberRepository extends JpaRepository<OrganizationMemberJpa, Long> {
    List<OrganizationMemberJpa> findAllByOrganizationId(Long organizationId);
    List<OrganizationMemberJpa> findAllByOrganizationIdAndStatus(Long organizationId, MemberStatus status);
}
