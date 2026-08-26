package com.atlashub.admin.infrastructure.persistence.repository;

import com.atlashub.admin.infrastructure.persistence.entity.AdminJpaEntity;
import com.atlashub.admin.domain.model.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataAdminRepository extends JpaRepository<AdminJpaEntity, Long> {
    Optional<AdminJpaEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRole(AdminRole role);
}
