package com.atlashub.compliance.application.commands.ReconcileAnchorCompliance;

import com.atlashub.compliance.application.port.AnchorCompliancePort;
import com.atlashub.compliance.domain.entities.ComplianceRecord;
import com.atlashub.compliance.domain.repositories.ComplianceRecordRepository;
import com.atlashub.compliance.domain.valueobject.ComplianceStatus;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import java.util.Locale;
import java.util.stream.Stream;
import com.atlashub.compliance.application.commands.OrchestrateAnchorCompliance.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ReconcileAnchorComplianceHandler extends Command<ReconcileAnchorComplianceCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(ReconcileAnchorComplianceHandler.class);
    private final ComplianceRecordRepository repository;
    private final AnchorCompliancePort anchor;
    private final OrchestrateAnchorComplianceHandler orchestrator;
    public ReconcileAnchorComplianceHandler(ComplianceRecordRepository repository, AnchorCompliancePort anchor,
                                            OrchestrateAnchorComplianceHandler orchestrator) {
        this.repository = repository; this.anchor = anchor; this.orchestrator = orchestrator;
    }
    @Override public Void execute(ReconcileAnchorComplianceCommand command) {
        repository.findAllByStatus(ComplianceStatus.SUBMITTED, 0, command.batchSize()).content()
                .forEach(record -> {
                    try { orchestrator.execute(new OrchestrateAnchorComplianceCommand(record.getOrganizationId())); }
                    catch (RuntimeException failure) {
                        log.warn("Anchor compliance submission will be retried for organization {} ({})",
                                record.getOrganizationId(), failure.getClass().getSimpleName());
                    }
                });
        Stream.of(ComplianceStatus.UNDER_REVIEW, ComplianceStatus.ACTION_REQUIRED)
                .flatMap(status -> repository.findAllByStatus(status, 0, command.batchSize()).content().stream())
                .filter(record -> record.getAnchorBusinessCustomerId() != null)
                .forEach(this::reconcile);
        return null;
    }
    private void reconcile(ComplianceRecord record) {
        try {
            var details = anchor.fetchBusinessCustomer(record.getAnchorBusinessCustomerId());
            if (details.verificationStatus() == null) return;
            switch (details.verificationStatus().toUpperCase(Locale.ROOT)) {
                case "APPROVED", "VERIFIED" -> record.recordAnchorApproved();
                case "REJECTED", "FAILED" -> record.recordAnchorRejected("Anchor reconciliation confirmed rejection");
                default -> { return; }
            }
        } catch (RuntimeException providerFailure) {
            record.recordAnchorError("ANCHOR_RECONCILIATION_FAILED", "Anchor reconciliation will be retried");
        }
        repository.save(record);
    }
}
