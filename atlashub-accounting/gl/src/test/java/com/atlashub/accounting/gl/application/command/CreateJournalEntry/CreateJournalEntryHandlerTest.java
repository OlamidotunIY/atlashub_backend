package com.atlashub.accounting.gl.application.command.CreateJournalEntry;

import com.atlashub.accounting.gl.domain.entities.Account;
import com.atlashub.accounting.gl.domain.entities.JournalEntry;
import com.atlashub.accounting.gl.domain.exceptions.AccountNotFoundException;
import com.atlashub.accounting.gl.domain.repositories.AccountRepository;
import com.atlashub.accounting.gl.domain.repositories.JournalEntryRepository;
import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import com.atlashub.accounting.gl.domain.valueobject.EntrySource;
import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.accounting.gl.domain.valueobject.JournalEntryStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateJournalEntryHandlerTest {

    private JournalEntryRepository journalEntryRepository;
    private AccountRepository accountRepository;
    private CreateJournalEntryHandler handler;

    @BeforeEach
    void setUp() {
        journalEntryRepository = mock(JournalEntryRepository.class);
        accountRepository = mock(AccountRepository.class);
        handler = new CreateJournalEntryHandler(journalEntryRepository, accountRepository);
    }

    @Test
    void execute_createsDraftJournalEntryWithLines() {
        Account cash = Account.create(1L, 10L, "1000", "Cash", AccountType.ASSET, null, false);
        Account revenue = Account.create(2L, 10L, "4000", "Revenue", AccountType.REVENUE, null, false);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(cash));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(revenue));
        when(journalEntryRepository.nextIdentity()).thenReturn(100L, 101L, 102L);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<JournalLineInput> lines = List.of(
                new JournalLineInput(1L, Money.of(new BigDecimal("500.00"), CurrencyCode.NGN), EntryType.DEBIT),
                new JournalLineInput(2L, Money.of(new BigDecimal("500.00"), CurrencyCode.NGN), EntryType.CREDIT)
        );

        CreateJournalEntryCommand command = new CreateJournalEntryCommand(
                10L,
                "JE-001",
                LocalDate.now(),
                "REF-1",
                "Sales payment",
                EntrySource.MANUAL,
                1001L,
                lines
        );

        CreateJournalEntryResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(100L, result.entryId());
        assertEquals("JE-001", result.entryNumber());
        assertEquals(JournalEntryStatus.DRAFT, result.status());
        assertEquals(2, result.lineCount());

        verify(journalEntryRepository).save(any(JournalEntry.class));
    }

    @Test
    void execute_accountNotFound_throwsAccountNotFoundException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        List<JournalLineInput> lines = List.of(
                new JournalLineInput(999L, Money.of(new BigDecimal("100.00"), CurrencyCode.NGN), EntryType.DEBIT)
        );

        CreateJournalEntryCommand command = new CreateJournalEntryCommand(
                10L,
                "JE-002",
                LocalDate.now(),
                "REF-2",
                "Invalid line account",
                EntrySource.MANUAL,
                1001L,
                lines
        );

        assertThrows(AccountNotFoundException.class, () -> handler.execute(command));
    }
}
