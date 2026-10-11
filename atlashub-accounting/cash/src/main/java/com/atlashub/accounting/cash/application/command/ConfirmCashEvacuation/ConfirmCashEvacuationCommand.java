package com.atlashub.accounting.cash.application.command.ConfirmCashEvacuation;

public record ConfirmCashEvacuationCommand(
        Long organizationId,
        Long evacuationId
) {}
