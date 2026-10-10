package com.atlashub.accounting.gl.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateJournalEntryRequest(
        @NotBlank @Size(max = 64) String entryNumber,
        @NotNull LocalDate date,
        @Size(max = 128) String reference,
        String description,
        @NotEmpty List<@Valid JournalLineRequest> lines
) {}
