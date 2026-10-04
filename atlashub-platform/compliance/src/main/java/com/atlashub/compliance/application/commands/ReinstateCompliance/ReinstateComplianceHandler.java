package com.atlashub.compliance.application.commands.ReinstateCompliance;
import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
@Component
public class ReinstateComplianceHandler extends Command<ReinstateComplianceCommand, Void> {
    private final ComplianceRecordRepository repository;
    public ReinstateComplianceHandler(ComplianceRecordRepository repository) { this.repository = repository; }
    @Override public Void execute(ReinstateComplianceCommand command) {
        var record = repository.findByOrganizationId(command.organizationId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found"));
        record.reinstate(command.reason()); repository.save(record); return null;
    }
}
