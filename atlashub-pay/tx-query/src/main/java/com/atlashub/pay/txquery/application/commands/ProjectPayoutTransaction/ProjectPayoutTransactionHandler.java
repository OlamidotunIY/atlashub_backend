package com.atlashub.pay.txquery.application.commands.ProjectPayoutTransaction;

import com.atlashub.pay.txquery.application.services.TransactionProjectionSupport;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ProjectPayoutTransactionHandler extends Command<ProjectPayoutTransactionCommand, Void> {
    private final TransactionProjectionSupport support;
    public ProjectPayoutTransactionHandler(TransactionProjectionSupport support) { this.support = support; }
    @Override public Void execute(ProjectPayoutTransactionCommand c) {
        support.upsert(c.organizationId(), c.environment(), TransactionType.PAYOUT, c.status(), c.amount(),
                "BANK_TRANSFER", null, c.reference(), c.sourceSystem(), c.sourceReferenceId(), null, null, null,
                c.recipientName(), c.recipientAccountNumber(), "Payout", Map.of(), c.occurredAt());
        return null;
    }
}
