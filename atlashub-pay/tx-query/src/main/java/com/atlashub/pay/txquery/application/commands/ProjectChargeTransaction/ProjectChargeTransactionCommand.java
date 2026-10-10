package com.atlashub.pay.txquery.application.commands.ProjectChargeTransaction;

import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;

public record ProjectChargeTransactionCommand(
        Long organizationId, String environment, String reference, Money amount, Money fee,
        String channel, String provider, String sourceSystem, String sourceReferenceId,
        String customerReferenceId, TransactionStatus status, String description, ZonedDateTime occurredAt) {
}
