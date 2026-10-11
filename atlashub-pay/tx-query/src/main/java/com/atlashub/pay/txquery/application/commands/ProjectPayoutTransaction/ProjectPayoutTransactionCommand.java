package com.atlashub.pay.txquery.application.commands.ProjectPayoutTransaction;

import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;
import java.util.Map;

public record ProjectPayoutTransactionCommand(Long organizationId, String environment, String reference, Money amount,
                                             String sourceSystem, String sourceReferenceId, String recipientName,
                                             String recipientAccountNumber, TransactionStatus status,
                                             String description, Map<String, String> metadata,
                                             ZonedDateTime occurredAt) {

    public ProjectPayoutTransactionCommand(Long organizationId, String environment, String reference, Money amount,
                                           String sourceSystem, String sourceReferenceId, String recipientName,
                                           String recipientAccountNumber, TransactionStatus status,
                                           ZonedDateTime occurredAt) {
        this(organizationId, environment, reference, amount, sourceSystem, sourceReferenceId, recipientName,
                recipientAccountNumber, status, null, Map.of(), occurredAt);
    }
}
