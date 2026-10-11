package com.atlashub.accounting.gl.infrastructure.persistence.adapters;

import com.atlashub.accounting.gl.domain.valueobject.AccountType;
import com.atlashub.accounting.gl.domain.valueobject.EntryType;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.AccountJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.BalanceSnapshotJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.entities.JournalLineJpa;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataAccountRepository;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataBalanceSnapshotRepository;
import com.atlashub.accounting.gl.infrastructure.persistence.repositories.SpringDataJournalLineRepository;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlAccountQueryAdapterTest {

    private SpringDataAccountRepository accountRepository;
    private SpringDataBalanceSnapshotRepository balanceSnapshotRepository;
    private SpringDataJournalLineRepository journalLineRepository;
    private GlAccountQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        accountRepository = mock(SpringDataAccountRepository.class);
        balanceSnapshotRepository = mock(SpringDataBalanceSnapshotRepository.class);
        journalLineRepository = mock(SpringDataJournalLineRepository.class);
        adapter = new GlAccountQueryAdapter(accountRepository, balanceSnapshotRepository, journalLineRepository);
    }

    @Test
    void getAccountBalance_accountNotFound_returnsEmpty() {
        when(accountRepository.findByOrganizationIdAndCode(10L, "1001")).thenReturn(Optional.empty());

        Optional<Money> balance = adapter.getAccountBalance(10L, "1001", LocalDate.now());

        assertFalse(balance.isPresent());
    }

    @Test
    void getAccountBalance_snapshotNotFound_returnsEmpty() {
        AccountJpa account = new AccountJpa(1L, 10L, "1001", "Bank", AccountType.ASSET, null, false, true, ZonedDateTime.now(), 0L);
        LocalDate asOf = LocalDate.now();

        when(accountRepository.findByOrganizationIdAndCode(10L, "1001")).thenReturn(Optional.of(account));
        when(balanceSnapshotRepository.findTopByAccountIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(1L, asOf))
                .thenReturn(Optional.empty());

        Optional<Money> balance = adapter.getAccountBalance(10L, "1001", asOf);

        assertFalse(balance.isPresent());
    }

    @Test
    void getAccountBalance_snapshotFound_returnsMoney() {
        AccountJpa account = new AccountJpa(1L, 10L, "1001", "Bank", AccountType.ASSET, null, false, true, ZonedDateTime.now(), 0L);
        LocalDate asOf = LocalDate.now();
        BalanceSnapshotJpa snapshot = new BalanceSnapshotJpa(
                100L, 1L, asOf, new BigDecimal("50000.00"), "NGN", ZonedDateTime.now(), 0L
        );

        when(accountRepository.findByOrganizationIdAndCode(10L, "1001")).thenReturn(Optional.of(account));
        when(balanceSnapshotRepository.findTopByAccountIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(1L, asOf))
                .thenReturn(Optional.of(snapshot));

        Optional<Money> balance = adapter.getAccountBalance(10L, "1001", asOf);

        assertTrue(balance.isPresent());
        assertEquals(Money.of("50000.00", CurrencyCode.NGN), balance.get());
    }

    @Test
    void getJournalLineIds_accountNotFound_returnsEmptyList() {
        when(accountRepository.findByOrganizationIdAndCode(10L, "1001")).thenReturn(Optional.empty());

        List<Long> ids = adapter.getJournalLineIds(10L, "1001", LocalDate.now());

        assertTrue(ids.isEmpty());
    }

    @Test
    void getJournalLineIds_accountFound_returnsLineIds() {
        AccountJpa account = new AccountJpa(1L, 10L, "1001", "Bank", AccountType.ASSET, null, false, true, ZonedDateTime.now(), 0L);
        JournalLineJpa line1 = new JournalLineJpa(101L, 10L, 1L, new BigDecimal("100.00"), "NGN", EntryType.DEBIT, 0L);
        JournalLineJpa line2 = new JournalLineJpa(102L, 10L, 1L, new BigDecimal("200.00"), "NGN", EntryType.CREDIT, 0L);

        when(accountRepository.findByOrganizationIdAndCode(10L, "1001")).thenReturn(Optional.of(account));
        when(journalLineRepository.findAllByAccountId(1L)).thenReturn(List.of(line1, line2));

        List<Long> ids = adapter.getJournalLineIds(10L, "1001", LocalDate.now());

        assertEquals(List.of(101L, 102L), ids);
    }
}
