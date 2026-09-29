package com.atlashub.iam.infrastructure.persistence.repositories;

import com.atlashub.iam.infrastructure.persistence.entities.PermissionJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataPermissionRepository extends JpaRepository<PermissionJpa, Long> {
    List<PermissionJpa> findByModule(String module);
}
