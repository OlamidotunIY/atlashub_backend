package com.atlashub.pay.txquery.application.commands.ProjectSettlementTransaction;

import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public record ProjectSettlementTransactionCommand(Long organizationId, String environment, String reference,
                                                  Money amount, String provider, TransactionStatus status,
                                                  ZonedDateTime occurredAt) {
}
