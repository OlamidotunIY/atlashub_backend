package com.atlashub.accounting.cash.application.command.RecordCashEvacuation;

import com.atlashub.shared.domain.valueobject.Money;

public record RecordCashEvacuationCommand(
        Long organizationId,
        Long outletId,
        Long tillId,
        Money amount,
        Long evacuatedBy
) {}
