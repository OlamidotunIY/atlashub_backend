package com.atlashub.pay.transfers.presentation.dto;

import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InitiatePayoutRequest(
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String recipientBankCode,
        @NotBlank String recipientAccountNumber,
        String recipientAccountName,
        @NotBlank String narration,
        @NotBlank String reference,
        @NotNull PaymentProvider provider,
        @NotNull SourceSystem sourceSystem,
        String sourceReferenceId
) {
}
