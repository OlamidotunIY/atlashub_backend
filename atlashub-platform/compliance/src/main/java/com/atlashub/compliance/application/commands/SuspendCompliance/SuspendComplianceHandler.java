package com.atlashub.compliance.application.commands.SuspendCompliance;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
@Component
public class SuspendComplianceHandler extends Command<SuspendComplianceCommand, Void> {
    private final ComplianceRecordRepository repository;
    public SuspendComplianceHandler(ComplianceRecordRepository repository) { this.repository = repository; }
    @Override public Void execute(SuspendComplianceCommand command) {
        var record = repository.findByOrganizationId(command.organizationId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found"));
        record.suspend(command.reason()); repository.save(record); return null;
    }
}
