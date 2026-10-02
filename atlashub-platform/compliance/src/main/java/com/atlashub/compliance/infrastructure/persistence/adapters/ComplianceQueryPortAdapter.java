package com.atlashub.compliance.infrastructure.persistence.adapters;

import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.infrastructure.persistence.mappers.ComplianceRecordMapper;
import com.atlashub.compliance.infrastructure.persistence.repositories.SpringDataComplianceRecordRepository;
import com.atlashub.shared.application.port.ComplianceQueryPort;
import org.springframework.stereotype.Component;

import static com.atlashub.compliance.domain.valueobject.ComplianceStatus.APPROVED;

@Component
public class ComplianceQueryPortAdapter implements ComplianceQueryPort {

    private final SpringDataComplianceRecordRepository repository;
    private final ComplianceRecordMapper mapper;

    public ComplianceQueryPortAdapter(SpringDataComplianceRecordRepository repository, ComplianceRecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public boolean isApproved(Long organizationId) {
        ComplianceRecord record = repository.findByOrganizationId(organizationId).map(mapper::toDomain).orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + organizationId));

        return record.getStatus() == APPROVED;
    }

    @Override
    public ComplianceStatus getStatus(Long organizationId) {
        ComplianceRecord record = repository.findByOrganizationId(organizationId).map(mapper::toDomain).orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found for org: " + organizationId));
        return ComplianceQueryPort.ComplianceStatus.valueOf(record.getStatus().name());
    }
}
