package com.atlashub.pay.txquery.application.commands.ProjectChargeTransaction;

import com.atlashub.pay.txquery.application.services.TransactionProjectionSupport;
import com.atlashub.pay.txquery.domain.entities.TransactionRecord;
import com.atlashub.pay.txquery.domain.valueobject.TransactionType;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProjectChargeTransactionHandler extends Command<ProjectChargeTransactionCommand, Void> {
    private final TransactionProjectionSupport support;

    public ProjectChargeTransactionHandler(TransactionProjectionSupport support) {
        this.support = support;
    }

    @Override
    public Void execute(ProjectChargeTransactionCommand command) {
        TransactionRecord record = support.upsert(command.organizationId(), command.environment(),
                command.status().name().startsWith("REFUND") ? TransactionType.REFUND : TransactionType.CHARGE,
                command.status(), command.amount(), command.channel(), command.provider(), command.reference(),
                command.sourceSystem(), command.sourceReferenceId(), "CUSTOMER", command.customerReferenceId(),
                null, null, null, command.description(), Map.of(), command.occurredAt());
        if (command.fee() != null) record.applyAmounts(command.fee(), command.amount().subtract(command.fee()));
        support.repository().save(record);
        return null;
    }
}
