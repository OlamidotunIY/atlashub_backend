package com.atlashub.accounting.gl.presentation.dto;

import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record JournalEntryResponse(
        Long id,
        Long organizationId,
        String entryNumber,
        LocalDate date,
        String reference,
        String description,
        List<JournalLineResponse> lines,
        JournalEntryStatus status,
        EntrySource source,
        BigDecimal totalDebits,
        BigDecimal totalCredits,
        String currency,
        Long initiatedBy,
        Long approvedBy,
        ZonedDateTime approvedAt,
        ZonedDateTime createdAt
) {}
