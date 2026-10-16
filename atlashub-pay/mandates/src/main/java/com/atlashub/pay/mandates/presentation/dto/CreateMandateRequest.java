package com.atlashub.pay.mandates.presentation.dto;

import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateMandateRequest(
        @NotBlank String customerId,
        @Email @NotBlank String email,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotNull MandateFrequency frequency,
        @NotBlank String authorizationCode,
        @NotNull LocalDate firstChargeDate
) {}
