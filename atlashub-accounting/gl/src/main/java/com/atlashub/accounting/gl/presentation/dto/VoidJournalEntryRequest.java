package com.atlashub.accounting.gl.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoidJournalEntryRequest(
        @NotBlank @Size(max = 255) String reason
) {}
