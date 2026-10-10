package com.atlashub.pay.txquery.application.commands.ProjectPayoutTransaction;

import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record ProjectPayoutTransactionCommand(Long organizationId, String environment, String reference, Money amount,
                                             String sourceSystem, String sourceReferenceId, String recipientName,
                                             String recipientAccountNumber, TransactionStatus status,
                                             ZonedDateTime occurredAt) {
}
