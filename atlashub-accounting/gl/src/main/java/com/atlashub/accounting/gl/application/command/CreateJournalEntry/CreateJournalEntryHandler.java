package com.atlashub.accounting.gl.application.command.CreateJournalEntry;

import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.AccountNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class CreateJournalEntryHandler extends Command<CreateJournalEntryCommand, CreateJournalEntryResult> {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;

    public CreateJournalEntryHandler(JournalEntryRepository journalEntryRepository,
                                    AccountRepository accountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('accounting:journal:create')")
    public CreateJournalEntryResult execute(CreateJournalEntryCommand command) {
        Long entryId = journalEntryRepository.nextIdentity();
        JournalEntry entry = JournalEntry.create(
                entryId,
                command.organizationId(),
                command.entryNumber(),
                command.date(),
                command.reference(),
                command.description(),
                command.source(),
                command.initiatedBy()
        );

        if (command.lines() != null) {
            for (JournalLineInput line : command.lines()) {
                boolean accountExists = accountRepository.findById(line.accountId())
                        .filter(a -> a.getOrganizationId().equals(command.organizationId()))
                        .isPresent();
                if (!accountExists) {
                    throw new AccountNotFoundException(line.accountId());
                }

                Long lineId = journalEntryRepository.nextIdentity();
                entry.addLine(lineId, line.accountId(), line.amount(), line.type());
            }
        }

        JournalEntry saved = journalEntryRepository.save(entry);

        return new CreateJournalEntryResult(
                saved.getId(),
                saved.getEntryNumber(),
                saved.getStatus(),
                saved.getLines().size()
        );
    }
}
