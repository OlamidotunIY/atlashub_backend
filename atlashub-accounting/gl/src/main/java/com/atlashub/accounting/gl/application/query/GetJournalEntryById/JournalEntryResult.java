package com.atlashub.accounting.gl.application.query.GetJournalEntryById;

import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public record JournalEntryResult(
        Long id,
        Long organizationId,
        String entryNumber,
        LocalDate date,
        String reference,
        String description,
        List<JournalLineResult> lines,
        JournalEntryStatus status,
        EntrySource source,
        Money totalDebits,
        Money totalCredits,
        Long initiatedBy,
        Long approvedBy,
        ZonedDateTime approvedAt,
        ZonedDateTime createdAt
) {}
