package com.atlashub.pay.txquery.application.commands.ProjectSettlementTransaction;

import com.atlashub.pay.txquery.application.services.TransactionProjectionSupport;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ProjectSettlementTransactionHandler extends Command<ProjectSettlementTransactionCommand, Void> {
    private final TransactionProjectionSupport support;
    public ProjectSettlementTransactionHandler(TransactionProjectionSupport support) { this.support = support; }
    @Override public Void execute(ProjectSettlementTransactionCommand c) {
        support.upsert(c.organizationId(), c.environment(), TransactionType.SETTLEMENT, c.status(), c.amount(),
                "BANK_TRANSFER", c.provider(), c.reference(), "SETTLEMENT", c.reference(), null, null, null,
                null, null, "Provider settlement", Map.of(), c.occurredAt());
        return null;
    }
}
