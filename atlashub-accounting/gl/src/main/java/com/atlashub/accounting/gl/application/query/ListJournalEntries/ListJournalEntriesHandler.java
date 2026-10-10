package com.atlashub.accounting.gl.application.query.ListJournalEntries;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListJournalEntriesHandler extends Query<ListJournalEntriesQuery, List<JournalEntrySummaryResult>> {

    private final JournalEntryRepository journalEntryRepository;

    public ListJournalEntriesHandler(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:journal:read')")
    public List<JournalEntrySummaryResult> execute(ListJournalEntriesQuery query) {
        List<JournalEntry> entries = query.status() != null
                ? journalEntryRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status())
                : journalEntryRepository.findByOrganizationId(query.organizationId());

        return entries.stream()
                .map(this::toSummary)
                .toList();
    }

    private JournalEntrySummaryResult toSummary(JournalEntry entry) {
        return new JournalEntrySummaryResult(
                entry.getId(),
                entry.getOrganizationId(),
                entry.getEntryNumber(),
                entry.getDate(),
                entry.getReference(),
                entry.getDescription(),
                entry.getLines().size(),
                entry.getStatus(),
                entry.getSource(),
                entry.calculateTotalDebits(),
                entry.calculateTotalCredits(),
                entry.getInitiatedBy(),
                entry.getApprovedBy(),
                entry.getApprovedAt(),
                entry.getCreatedAt()
        );
    }
}
