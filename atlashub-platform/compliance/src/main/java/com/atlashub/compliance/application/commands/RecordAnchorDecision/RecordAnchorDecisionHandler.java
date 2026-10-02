package com.atlashub.compliance.application.commands.RecordAnchorDecision;

import com.atlashub.compliance.domain.exception.ComplianceRecordNotFoundException;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecordAnchorDecisionHandler extends Command<RecordAnchorDecisionCommand, Void> {
    private final ComplianceRecordRepository repository;
    public RecordAnchorDecisionHandler(ComplianceRecordRepository repository) { this.repository = repository; }
    @Override @Transactional public Void execute(RecordAnchorDecisionCommand command) {
        var record = repository.findByOrganizationId(command.organizationId())
                .orElseThrow(() -> new ComplianceRecordNotFoundException("Compliance record not found"));
        if (record.getAnchorBusinessCustomerId() == null) {
            record.recordAnchorCustomerCreated(command.anchorBusinessCustomerId());
        } else if (!record.getAnchorBusinessCustomerId().equals(command.anchorBusinessCustomerId())) {
            throw new IllegalArgumentException("Anchor customer does not match the compliance record");
        }
        if (command.approved()) record.recordAnchorApproved();
        else record.recordAnchorRejected(command.reason());
        repository.save(record);
        return null;
    }
}
