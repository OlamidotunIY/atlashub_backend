package com.atlashub.accounting.ar.application.command.CreateCustomerReceivable;

import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;

public record CreateCustomerReceivableCommand(
        Long organizationId,
        Long customerId,
        Long salesOrderId,
        Money amount,
        LocalDate dueDate
) {}
