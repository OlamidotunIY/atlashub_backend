package com.atlashub.pay.accounts.infrastructure.messaging.schedulers;

import com.atlashub.pay.accounts.application.commands.ReconcileAnchorAccounts.ReconcileAnchorAccountsCommand;
import com.atlashub.pay.accounts.application.commands.ReconcileAnchorAccounts.ReconcileAnchorAccountsHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class AnchorAccountReconciliationScheduler {
    private final ReconcileAnchorAccountsHandler handler;

    public AnchorAccountReconciliationScheduler(ReconcileAnchorAccountsHandler handler) {
        this.handler = handler;
    }

    @Scheduled(cron = "${pay.accounts.reconciliation-cron:0 */5 * * * *}")
    public void reconcile() {
        handler.execute(new ReconcileAnchorAccountsCommand(100));
    }
}
