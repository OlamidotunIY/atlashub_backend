package com.atlashub.compliance.infrastructure.persistence.repositories;

import com.atlashub.compliance.infrastructure.persistence.entities.BusinessOfficerJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataBusinessOfficerRepository extends JpaRepository<BusinessOfficerJpa, Long> {
    List<BusinessOfficerJpa> findAllByComplianceRecordId(Long complianceRecordId);
    void deleteAllByComplianceRecordId(Long complianceRecordId);
}
