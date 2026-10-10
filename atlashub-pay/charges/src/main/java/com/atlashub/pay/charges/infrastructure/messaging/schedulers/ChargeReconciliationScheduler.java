package com.atlashub.pay.charges.infrastructure.messaging.schedulers;

import com.atlashub.pay.charges.application.commands.ReconcilePendingCharges.ReconcilePendingChargesCommand;
import com.atlashub.pay.charges.application.commands.ReconcilePendingCharges.ReconcilePendingChargesHandler;
import com.atlashub.pay.charges.application.commands.ReconcilePendingCharges.ReconcilePendingChargesResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ChargeReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ChargeReconciliationScheduler.class);

    private final ReconcilePendingChargesHandler handler;

    public ChargeReconciliationScheduler(ReconcilePendingChargesHandler handler) {
        this.handler = handler;
    }

    @Scheduled(cron = "${atlashub.pay.charges.reconciliation-cron:0 */15 * * * *}")
    public void run() {
        log.info("Starting scheduled reconciliation for pending charges");
        ReconcilePendingChargesResult result = handler.execute(new ReconcilePendingChargesCommand());
        log.info("Finished scheduled reconciliation: {} reconciled, {} failures", result.reconciledCount(),
                result.failureCount());
    }
}
