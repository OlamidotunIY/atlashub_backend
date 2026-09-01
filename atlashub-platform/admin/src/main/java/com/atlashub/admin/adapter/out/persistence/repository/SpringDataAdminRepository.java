package com.atlashub.admin.adapter.out.persistence.repository;

import com.atlashub.admin.adapter.out.persistence.entity.AdminJpaEntity;
import com.atlashub.admin.domain.valueobject.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataAdminRepository extends JpaRepository<AdminJpaEntity, Long> {
    Optional<AdminJpaEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByRole(AdminRole role);
}
