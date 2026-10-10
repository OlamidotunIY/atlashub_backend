package com.atlashub.accounting.gl.application.query.GetJournalEntryById;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.JournalEntryNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetJournalEntryByIdHandler extends Query<GetJournalEntryByIdQuery, JournalEntryResult> {

    private final JournalEntryRepository journalEntryRepository;

    public GetJournalEntryByIdHandler(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:journal:read')")
    public JournalEntryResult execute(GetJournalEntryByIdQuery query) {
        JournalEntry entry = journalEntryRepository.findById(query.entryId())
                .filter(e -> e.getOrganizationId().equals(query.organizationId()))
                .orElseThrow(() -> new JournalEntryNotFoundException(query.entryId()));

        List<JournalLineResult> lines = entry.getLines().stream()
                .map(line -> new JournalLineResult(
                        line.getId(),
                        line.getAccountId(),
                        line.getAmount(),
                        line.getType()
                ))
                .toList();

        return new JournalEntryResult(
                entry.getId(),
                entry.getOrganizationId(),
                entry.getEntryNumber(),
                entry.getDate(),
                entry.getReference(),
                entry.getDescription(),
                lines,
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
