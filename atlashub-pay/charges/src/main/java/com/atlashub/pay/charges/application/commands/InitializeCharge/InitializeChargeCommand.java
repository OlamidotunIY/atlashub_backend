package com.atlashub.pay.charges.application.commands.InitializeCharge;

import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Money;

import java.util.Map;

public record InitializeChargeCommand(
        Long organizationId,
        ApiEnvironment environment,
        String reference,
        Money amount,
        ChargeChannel channel,
        String email,
        String sourceSystem,
        String sourceReferenceId,
        String customerReferenceId,
        Long terminalAssignmentId,
        Map<String, Object> metadata
) {
}
