package com.atlashub.accounting.cash.application.command;

import com.atlashub.accounting.cash.application.command.StartBankReconciliation.BankStatementLineInput;
import com.atlashub.accounting.cash.application.command.StartBankReconciliation.StartBankReconciliationCommand;
import com.atlashub.accounting.cash.application.command.StartBankReconciliation.StartBankReconciliationHandler;
import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.shared.application.port.GlAccountQueryPort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartBankReconciliationHandlerTest {

    private BankReconciliationRepository repository;
    private GlAccountQueryPort glAccountQueryPort;
    private StartBankReconciliationHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(BankReconciliationRepository.class);
        glAccountQueryPort = mock(GlAccountQueryPort.class);
        handler = new StartBankReconciliationHandler(repository, glAccountQueryPort);
    }

    @Test
    void execute_createsAndSavesBankReconciliationWithGlBalance() {
        LocalDate statementDate = LocalDate.now();
        Money statementClosingBalance = Money.of("100000.00", CurrencyCode.NGN);
        Money bookBalance = Money.of("80000.00", CurrencyCode.NGN);

        when(repository.nextIdentity()).thenReturn(1L, 101L, 102L);
        when(glAccountQueryPort.getAccountBalance(10L, "1001", statementDate)).thenReturn(Optional.of(bookBalance));
        when(glAccountQueryPort.getJournalLineIds(10L, "1001", statementDate)).thenReturn(List.of(201L, 202L));
        when(repository.save(any(BankReconciliation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<BankStatementLineInput> lines = List.of(
                new BankStatementLineInput(statementDate, "Deposit", Money.of("15000.00", CurrencyCode.NGN), StatementEntryType.CREDIT),
                new BankStatementLineInput(statementDate, "Fee", Money.of("5000.00", CurrencyCode.NGN), StatementEntryType.DEBIT)
        );

        StartBankReconciliationCommand command = new StartBankReconciliationCommand(
                10L, "1001", statementDate, statementClosingBalance, lines
        );

        Long resultId = handler.execute(command);

        assertEquals(1L, resultId);

        ArgumentCaptor<BankReconciliation> captor = ArgumentCaptor.forClass(BankReconciliation.class);
        verify(repository).save(captor.capture());

        BankReconciliation saved = captor.getValue();
        assertEquals(1L, saved.getId());
        assertEquals(10L, saved.getOrganizationId());
        assertEquals("1001", saved.getBankAccountCode());
        assertEquals(statementDate, saved.getStatementDate());
        assertEquals(statementClosingBalance, saved.getStatementClosingBalance());
        assertEquals(bookBalance, saved.getBookBalance());
        assertEquals(Money.of("20000.00", CurrencyCode.NGN), saved.getDifference());
        assertEquals(ReconciliationStatus.DRAFT, saved.getStatus());
        assertEquals(2, saved.getUnmatchedBankItems().size());
        assertEquals(2, saved.getUnmatchedBookItems().size());
    }

    @Test
    void execute_glBalanceEmpty_defaultsToZeroBookBalance() {
        LocalDate statementDate = LocalDate.now();
        Money statementClosingBalance = Money.of("50000.00", CurrencyCode.NGN);

        when(repository.nextIdentity()).thenReturn(2L);
        when(glAccountQueryPort.getAccountBalance(10L, "1001", statementDate)).thenReturn(Optional.empty());
        when(glAccountQueryPort.getJournalLineIds(10L, "1001", statementDate)).thenReturn(List.of());
        when(repository.save(any(BankReconciliation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StartBankReconciliationCommand command = new StartBankReconciliationCommand(
                10L, "1001", statementDate, statementClosingBalance, List.of()
        );

        Long resultId = handler.execute(command);

        assertEquals(2L, resultId);

        ArgumentCaptor<BankReconciliation> captor = ArgumentCaptor.forClass(BankReconciliation.class);
        verify(repository).save(captor.capture());

        BankReconciliation saved = captor.getValue();
        assertEquals(Money.zero(CurrencyCode.NGN), saved.getBookBalance());
        assertEquals(statementClosingBalance, saved.getDifference());
    }
}
