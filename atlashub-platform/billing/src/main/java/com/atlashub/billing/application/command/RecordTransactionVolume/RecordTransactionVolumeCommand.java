package com.atlashub.billing.application.command.RecordTransactionVolume;

import java.math.BigDecimal;

public record RecordTransactionVolumeCommand(
        Long organizationId,
        String month,
        BigDecimal amount,
        String currency
) {
}
