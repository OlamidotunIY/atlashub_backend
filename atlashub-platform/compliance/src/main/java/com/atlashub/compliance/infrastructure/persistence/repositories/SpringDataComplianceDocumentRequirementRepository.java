package com.atlashub.compliance.infrastructure.persistence.repositories;

import com.atlashub.compliance.infrastructure.persistence.entities.ComplianceDocumentRequirementJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataComplianceDocumentRequirementRepository
        extends JpaRepository<ComplianceDocumentRequirementJpa, Long> {
    List<ComplianceDocumentRequirementJpa> findAllByComplianceRecordId(Long complianceRecordId);
    Optional<ComplianceDocumentRequirementJpa> findByAnchorDocumentId(String anchorDocumentId);
    void deleteAllByComplianceRecordId(Long complianceRecordId);
}
