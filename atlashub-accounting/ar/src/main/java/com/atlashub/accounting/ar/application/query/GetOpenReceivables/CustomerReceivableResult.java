package com.atlashub.accounting.ar.application.query.GetOpenReceivables;

import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record CustomerReceivableResult(
        Long id,
        Long organizationId,
        Long customerId,
        Long salesOrderId,
        Money totalAmount,
        Money paidAmount,
        Money outstandingBalance,
        LocalDate dueDate,
        ArStatus status,
        ZonedDateTime createdAt
) {}
