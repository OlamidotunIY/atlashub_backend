package com.atlashub.audit.infrastructure.persistence.repositories;

import com.atlashub.audit.infrastructure.persistence.entities.AuditLogJpa;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogJpa, Long> {

    List<AuditLogJpa> findByOrganizationIdOrderByTimestampDesc(Long organizationId, Pageable pageable);

    List<AuditLogJpa> findByOrganizationIdAndResourceTypeOrderByTimestampDesc(Long organizationId, String resourceType, Pageable pageable);
}
