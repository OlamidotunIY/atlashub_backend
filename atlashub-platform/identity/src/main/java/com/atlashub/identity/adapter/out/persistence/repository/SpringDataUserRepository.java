package com.atlashub.identity.adapter.out.persistence.repository;

import com.atlashub.identity.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByIntegrationAndEmail(Long OrganizationId, String email);
}
