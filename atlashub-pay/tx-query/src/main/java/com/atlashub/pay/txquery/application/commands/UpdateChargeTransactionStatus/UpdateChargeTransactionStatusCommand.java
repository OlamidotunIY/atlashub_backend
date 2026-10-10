package com.atlashub.pay.txquery.application.commands.UpdateChargeTransactionStatus;

import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import java.time.ZonedDateTime;

public record UpdateChargeTransactionStatusCommand(Long organizationId, String environment, String reference,
                                                   TransactionStatus status, String description,
                                                   ZonedDateTime occurredAt) {
}
