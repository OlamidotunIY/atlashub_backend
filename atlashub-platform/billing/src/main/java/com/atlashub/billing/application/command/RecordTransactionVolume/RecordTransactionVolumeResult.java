package com.atlashub.billing.application.command.RecordTransactionVolume;

import java.math.BigDecimal;

public record RecordTransactionVolumeResult(
        Long ledgerId,
        BigDecimal totalVolume,
        Long totalTransactionCount
) {
}
