package com.atlashub.accounting.gl.application.command.ApproveJournalEntry;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.JournalEntryNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class ApproveJournalEntryHandler extends Command<ApproveJournalEntryCommand, ApproveJournalEntryResult> {

    private final JournalEntryRepository journalEntryRepository;

    public ApproveJournalEntryHandler(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:journal:approve')")
    public ApproveJournalEntryResult execute(ApproveJournalEntryCommand command) {
        JournalEntry entry = journalEntryRepository.findById(command.entryId())
                .filter(e -> e.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(() -> new JournalEntryNotFoundException(command.entryId()));

        entry.approve(command.approverId());
        JournalEntry saved = journalEntryRepository.save(entry);

        return new ApproveJournalEntryResult(
                saved.getId(),
                saved.getEntryNumber(),
                saved.getStatus(),
                saved.getApprovedBy(),
                saved.getApprovedAt()
        );
    }
}
