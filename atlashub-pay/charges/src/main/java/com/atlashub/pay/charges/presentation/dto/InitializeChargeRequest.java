package com.atlashub.pay.charges.presentation.dto;

import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record InitializeChargeRequest(
        @NotBlank String reference,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,
        @NotNull CurrencyCode currency,
        @NotNull ChargeChannel channel,
        @NotBlank @Email String email,
        @NotBlank String sourceSystem,
        @NotBlank String sourceReferenceId,
        String customerReferenceId,
        Long terminalAssignmentId,
        Long providerConnectionId,
        Map<String, Object> metadata
) {
    public InitializeChargeRequest(String reference, BigDecimal amount, CurrencyCode currency, ChargeChannel channel,
                                   String email, String sourceSystem, String sourceReferenceId,
                                   String customerReferenceId, Long terminalAssignmentId,
                                   Map<String, Object> metadata) {
        this(reference, amount, currency, channel, email, sourceSystem, sourceReferenceId, customerReferenceId,
                terminalAssignmentId, null, metadata);
    }

    public InitializeChargeRequest {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
