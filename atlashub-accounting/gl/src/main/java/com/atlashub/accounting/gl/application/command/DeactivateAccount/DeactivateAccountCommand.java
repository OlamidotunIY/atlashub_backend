package com.atlashub.accounting.gl.application.command.DeactivateAccount;

public record DeactivateAccountCommand(
        Long accountId,
        Long organizationId
) {}
