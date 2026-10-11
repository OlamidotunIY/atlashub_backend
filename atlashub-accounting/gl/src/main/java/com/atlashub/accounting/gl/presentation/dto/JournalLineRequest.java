package com.atlashub.accounting.gl.presentation.dto;

import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record JournalLineRequest(
        @NotNull Long accountId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull EntryType type
) {}
