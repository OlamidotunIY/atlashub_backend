package com.atlashub.compliance.infrastructure.messaging.schedulers;

import com.atlashub.compliance.application.commands.ReconcileAnchorCompliance.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class AnchorComplianceReconciliationScheduler {
    private final ReconcileAnchorComplianceHandler handler;
    public AnchorComplianceReconciliationScheduler(ReconcileAnchorComplianceHandler handler) { this.handler = handler; }
    @Scheduled(cron = "${atlashub.compliance.anchor-reconciliation-cron:0 */15 * * * *}", zone = "UTC")
    public void reconcile() { handler.execute(new ReconcileAnchorComplianceCommand(100)); }
}
