package com.atlashub.accounting.gl.application.command.PostJournalEntry;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.JournalEntryNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class PostJournalEntryHandler extends Command<PostJournalEntryCommand, PostJournalEntryResult> {

    private final JournalEntryRepository journalEntryRepository;

    public PostJournalEntryHandler(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:journal:post')")
    public PostJournalEntryResult execute(PostJournalEntryCommand command) {
        JournalEntry entry = journalEntryRepository.findById(command.entryId())
                .filter(e -> e.getOrganizationId().equals(command.organizationId()))
                .orElseThrow(() -> new JournalEntryNotFoundException(command.entryId()));

        entry.post();
        JournalEntry saved = journalEntryRepository.save(entry);

        return new PostJournalEntryResult(
                saved.getId(),
                saved.getEntryNumber(),
                saved.getStatus(),
                saved.calculateTotalDebits(),
                saved.calculateTotalCredits()
        );
    }
}
