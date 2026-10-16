package com.atlashub.pay.transfers.presentation.dto;

import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record InitiateBulkPayoutRequest(
        @NotBlank String bulkReference,
        @NotNull SourceSystem sourceSystem,
        String sourceReferenceId,
        @NotEmpty List<BulkPayoutItemRequest> items
) {
    public record BulkPayoutItemRequest(
            @NotBlank String recipientBankCode,
            @NotBlank String recipientAccountNumber,
            String recipientAccountName,
            @NotNull BigDecimal amount,
            @NotBlank String currency,
            @NotBlank String narration,
            @NotBlank String itemReference
    ) {
    }
}
