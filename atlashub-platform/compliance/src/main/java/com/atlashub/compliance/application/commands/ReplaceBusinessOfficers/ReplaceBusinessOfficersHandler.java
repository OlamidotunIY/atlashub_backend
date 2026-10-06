package com.atlashub.compliance.application.commands.ReplaceBusinessOfficers;

import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.entities.BusinessOfficer;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class ReplaceBusinessOfficersHandler extends Command<ReplaceBusinessOfficersCommand, ComplianceRecord> {
    private final ComplianceRecordRepository repository;
    public ReplaceBusinessOfficersHandler(ComplianceRecordRepository repository) { this.repository = repository; }
    @Override @PreAuthorize("hasAuthority('compliance:manage')")
    public ComplianceRecord execute(ReplaceBusinessOfficersCommand command) {
        ComplianceRecord record = repository.findByOrganizationId(command.organizationId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found"));
        record.replaceOfficers(command.officers().stream().map(input -> new BusinessOfficer(
                repository.nextIdentity(), input.role(), input.firstName(), input.middleName(), input.lastName(),
                input.maidenName(), input.nationality(), input.dateOfBirth(), input.email(), input.phoneNumber(),
                input.address(), input.bvn(), input.title(), input.percentageOwned(), null, null)).toList());
        return repository.save(record);
    }
}
