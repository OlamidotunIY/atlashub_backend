package com.atlashub.accounting.gl.application.command.VoidJournalEntry;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.JournalEntryNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class VoidJournalEntryHandler extends Command<VoidJournalEntryCommand, VoidJournalEntryResult> {

    private final JournalEntryRepository journalEntryRepository;

    public VoidJournalEntryHandler(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:journal:void')")
    public VoidJournalEntryResult execute(VoidJournalEntryCommand command) {
        JournalEntry entry = journalEntryRepository.findById(command.entryId())
                .filter(e -> e.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(() -> new JournalEntryNotFoundException(command.entryId()));

        entry.voidEntry(command.voidedBy(), command.reason());
        JournalEntry saved = journalEntryRepository.save(entry);

        return new VoidJournalEntryResult(
                saved.getId(),
                saved.getEntryNumber(),
                saved.getStatus()
        );
    }
}
