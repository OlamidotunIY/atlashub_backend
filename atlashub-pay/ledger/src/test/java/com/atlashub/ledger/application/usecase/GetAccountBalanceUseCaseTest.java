package com.atlashub.ledger.application.usecase;

import com.atlashub.ledger.application.query.GetAccountBalanceQuery;
import com.atlashub.ledger.domain.model.BalanceSnapshot;
import com.atlashub.ledger.domain.valueobject.EntryType;
import com.atlashub.ledger.domain.model.LedgerEntry;
import com.atlashub.ledger.domain.model.TransactionReference;
import com.atlashub.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlashub.ledger.domain.repository.LedgerEntryRepository;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.atlashub.shared.application.port.out.AccountQueryPort;
import com.atlashub.shared.application.port.out.AccountDetailsDto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetAccountBalanceUseCaseTest {

    private BalanceSnapshotRepository snapshotRepository;
    private LedgerEntryRepository entryRepository;
    private AccountQueryPort accountQueryPort;
    private GetAccountBalanceUseCase useCase;

    @BeforeEach
    void setUp() {
        snapshotRepository = mock(BalanceSnapshotRepository.class);
        entryRepository = mock(LedgerEntryRepository.class);
        accountQueryPort = mock(AccountQueryPort.class);
        useCase = new GetAccountBalanceUseCase(snapshotRepository, entryRepository, accountQueryPort);
    }

    @Test
    void shouldReturnSnapshotBalanceWhenPresent() {
        GetAccountBalanceQuery query = new GetAccountBalanceQuery(10L, 100L);
        when(accountQueryPort.findAccountDetails(10L)).thenReturn(Optional.of(new AccountDetailsDto(10L, 100L, CurrencyCode.NGN, "ACTIVE")));

        BalanceSnapshot snapshot = new BalanceSnapshot(1L, 10L, Money.of("1000", CurrencyCode.NGN), 100L, ZonedDateTime.now());
        when(snapshotRepository.findLatestByAccountId(10L)).thenReturn(Optional.of(snapshot));

        Money result = useCase.execute(query);

        assertEquals(Money.of("1000.0000", CurrencyCode.NGN), result);
    }

    @Test
    void shouldReturnZeroWhenNoSnapshot() {
        GetAccountBalanceQuery query = new GetAccountBalanceQuery(10L, 100L);
        when(accountQueryPort.findAccountDetails(10L)).thenReturn(Optional.of(new AccountDetailsDto(10L, 100L, CurrencyCode.NGN, "ACTIVE")));

        when(snapshotRepository.findLatestByAccountId(10L)).thenReturn(Optional.empty());

        Money result = useCase.execute(query);

        assertEquals(Money.zero(CurrencyCode.NGN), result);
    }
}
