package com.atlashub.audit.domain.repositories;

import com.atlashub.audit.domain.entities.AuditLog;
import com.atlashub.shared.domain.repository.Repository;

import java.util.List;

public interface AuditLogRepository extends Repository<AuditLog> {

    List<AuditLog> findByOrganizationId(Long organizationId, int page, int size);

    List<AuditLog> findByOrganizationIdAndResourceType(Long organizationId, String resourceType, int page, int size);
}
