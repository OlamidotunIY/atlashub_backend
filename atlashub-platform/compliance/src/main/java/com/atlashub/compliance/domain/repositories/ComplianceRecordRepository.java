package com.atlashub.compliance.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import java.util.Optional;

import com.atlashub.shared.domain.valueobject.PageResult;
import com.atlashub.compliance.domain.valueobject.ComplianceStatus;

public interface ComplianceRecordRepository extends Repository<ComplianceRecord> {
    Optional<ComplianceRecord> findByOrganizationId(Long organizationId);
    PageResult<ComplianceRecord> findAllByStatus(ComplianceStatus status, int page, int size);
}
