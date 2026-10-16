package com.atlashub.accounting.ar.application.command.RecordReceivablePayment;

import com.atlashub.shared.domain.valueobject.Money;

public record RecordReceivablePaymentCommand(
        Long receivableId,
        Money amountPaid
) {}
