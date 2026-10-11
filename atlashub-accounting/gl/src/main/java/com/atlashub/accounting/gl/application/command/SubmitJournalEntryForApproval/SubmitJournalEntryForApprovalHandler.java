package com.atlashub.accounting.gl.application.command.SubmitJournalEntryForApproval;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.JournalEntryNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class SubmitJournalEntryForApprovalHandler
        extends Command<SubmitJournalEntryForApprovalCommand, SubmitJournalEntryForApprovalResult> {

    private final JournalEntryRepository journalEntryRepository;

    public SubmitJournalEntryForApprovalHandler(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:journal:create')")
    public SubmitJournalEntryForApprovalResult execute(SubmitJournalEntryForApprovalCommand command) {
        JournalEntry entry = journalEntryRepository.findById(command.entryId())
                .filter(e -> e.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(() -> new JournalEntryNotFoundException(command.entryId()));

        entry.submitForApproval(command.initiatorId());
        JournalEntry saved = journalEntryRepository.save(entry);

        return new SubmitJournalEntryForApprovalResult(
                saved.getId(),
                saved.getEntryNumber(),
                saved.getStatus()
        );
    }
}
