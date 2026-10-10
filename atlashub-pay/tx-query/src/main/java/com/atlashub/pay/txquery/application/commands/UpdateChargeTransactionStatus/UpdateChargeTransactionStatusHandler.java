package com.atlashub.pay.txquery.application.commands.UpdateChargeTransactionStatus;

import com.atlashub.pay.txquery.application.services.TransactionProjectionSupport;
import com.atlashub.pay.txquery.domain.exceptions.TransactionRecordNotFoundException;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;

@Component
public class UpdateChargeTransactionStatusHandler extends Command<UpdateChargeTransactionStatusCommand, Void> {
    private final TransactionProjectionSupport support;
    public UpdateChargeTransactionStatusHandler(TransactionProjectionSupport support) { this.support = support; }
    @Override public Void execute(UpdateChargeTransactionStatusCommand command) {
        var record = support.repository().findByOrganizationIdAndEnvironmentAndReference(command.organizationId(),
                ApiEnvironment.parse(command.environment()), command.reference())
                .orElseThrow(() -> new TransactionRecordNotFoundException(command.reference()));
        record.updateStatus(command.status(), command.occurredAt(), command.description());
        support.repository().save(record);
        return null;
    }
}
