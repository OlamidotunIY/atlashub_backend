package com.atlashub.pay.txquery.application.commands.ProjectAccountFunding;

import com.atlashub.pay.txquery.application.services.TransactionProjectionSupport;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ProjectAccountFundingHandler extends Command<ProjectAccountFundingCommand, Void> {
    private final TransactionProjectionSupport support;
    public ProjectAccountFundingHandler(TransactionProjectionSupport support) { this.support = support; }
    @Override public Void execute(ProjectAccountFundingCommand c) {
        support.upsert(c.organizationId(), c.environment(), TransactionType.ACCOUNT_FUNDING,
                TransactionStatus.SUCCESSFUL, c.amount(), "BANK_TRANSFER", c.provider(), c.reference(),
                "EXTERNAL_COLLECTION", c.accountId() == null ? null : c.accountId().toString(), c.partyType(),
                c.partyReferenceId(), null, null, null, "Account funding", Map.of(), c.occurredAt());
        return null;
    }
}
