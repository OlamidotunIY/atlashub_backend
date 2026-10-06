package com.atlashub.iam.infrastructure.persistence.repositories;

import com.atlashub.iam.infrastructure.persistence.entities.PermissionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataPermissionRepository extends JpaRepository<PermissionJpa, Long> {
    List<PermissionJpa> findByModule(String module);
    List<PermissionJpa> findAllByActiveTrue();
    Optional<PermissionJpa> findByCode(String code);
}
