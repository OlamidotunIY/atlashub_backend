package com.atlashub.accounting.gl.application.command.DeactivateAccount;

public record DeactivateAccountResult(
        Long accountId,
        Boolean isActive
) {}
