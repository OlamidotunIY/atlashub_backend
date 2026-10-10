package com.atlashub.pay.txquery.application.commands.ProjectLedgerPosting;

import com.atlashub.pay.txquery.application.services.TransactionProjectionSupport;
import com.atlashub.pay.txquery.domain.entities.TransactionAccountEntry;
import com.atlashub.pay.txquery.domain.valueobject.TransactionDirection;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ProjectLedgerPostingHandler extends Command<ProjectLedgerPostingCommand, Void> {
    private final TransactionProjectionSupport support;
    public ProjectLedgerPostingHandler(TransactionProjectionSupport support) { this.support = support; }

    @Override
    public Void execute(ProjectLedgerPostingCommand c) {
        CurrencyCode currency = CurrencyCode.valueOf(c.currency());
        BigDecimal amount = c.entries().stream().filter(e -> "DEBIT".equals(e.entryType()))
                .map(ProjectLedgerPostingCommand.Entry::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        var record = support.upsert(c.organizationId(), c.environment(), TransactionType.LEDGER_POST,
                TransactionStatus.SUCCESSFUL, Money.of(amount, currency), "INTERNAL", "INTERNAL", c.reference(),
                c.sourceSystem(), c.sourceReferenceId(), null, null, null, null, null, c.description(), Map.of(),
                c.postedAt());
        var projected = c.entries().stream().map(entry -> new TransactionAccountEntry(
                support.repository().nextEntryIdentity(), record.getId(), entry.accountId(), null, null,
                entry.entryType(), "CREDIT".equals(entry.entryType()) ? TransactionDirection.INCOMING
                : TransactionDirection.OUTGOING, Money.of(entry.amount(), currency))).toList();
        record.attachAccountEntries(projected);
        support.repository().save(record);
        return null;
    }
}
