package com.atlashub.identity.adapter.out.persistence.repository;

import com.atlashub.identity.adapter.out.persistence.entity.OrganizationMemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataOrganizationMemberRepository extends JpaRepository<OrganizationMemberJpaEntity, Long> {
    Optional<OrganizationMemberJpaEntity> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    List<OrganizationMemberJpaEntity> findByOrganizationId(Long organizationId);
    List<OrganizationMemberJpaEntity> findByUserId(Long userId);
}
