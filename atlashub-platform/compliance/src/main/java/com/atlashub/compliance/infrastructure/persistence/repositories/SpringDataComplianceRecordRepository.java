package com.atlashub.compliance.infrastructure.persistence.repositories;

import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import com.atlashub.compliance.infrastructure.persistence.entities.ComplianceRecordJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataComplianceRecordRepository extends JpaRepository<ComplianceRecordJpa, Long> {
    Optional<ComplianceRecordJpa> findByOrganizationId(Long organizationId);
    Page<ComplianceRecordJpa> findAllByStatus(ComplianceStatus status, Pageable pageable);
}
