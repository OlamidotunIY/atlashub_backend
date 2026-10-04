package com.atlashub.iam.infrastructure.persistence.repositories;

import com.atlashub.iam.domain.valueobject.MemberStatus;
import com.atlashub.iam.infrastructure.persistence.entities.OrganizationMemberJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface SpringDataOrganizationMemberRepository extends JpaRepository<OrganizationMemberJpa, Long> {
    List<OrganizationMemberJpa> findAllByOrganizationId(Long organizationId);
    List<OrganizationMemberJpa> findAllByOrganizationIdAndStatus(Long organizationId, MemberStatus status);
    Optional<OrganizationMemberJpa> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select member from OrganizationMemberJpa member where member.id = :id and member.organizationId = :organizationId")
    Optional<OrganizationMemberJpa> findByIdAndOrganizationIdForUpdate(
            @Param("id") Long id, @Param("organizationId") Long organizationId);
    List<OrganizationMemberJpa> findAllByUserId(Long userId);
    long countByOrganizationIdAndCustomRoleIdAndStatus(Long organizationId, Long customRoleId, MemberStatus status);
}
