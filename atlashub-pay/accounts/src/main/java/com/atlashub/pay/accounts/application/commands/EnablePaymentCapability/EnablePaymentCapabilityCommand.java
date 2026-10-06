package com.atlashub.pay.accounts.application.commands.EnablePaymentCapability;

public record EnablePaymentCapabilityCommand(
        Long organizationId,
        String environment,
        String capability,
        String terminalProvider
) {
}
