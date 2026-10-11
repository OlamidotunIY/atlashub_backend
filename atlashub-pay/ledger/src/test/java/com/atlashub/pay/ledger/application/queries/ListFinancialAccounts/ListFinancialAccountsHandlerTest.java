package com.atlashub.pay.ledger.application.queries.ListFinancialAccounts;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.NormalBalance;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.port.OrganizationQueryPort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFinancialAccountsHandlerTest {
    @Mock private LedgerAccountRepository accounts;
    @Mock private BalanceSnapshotRepository snapshots;
    @Mock private LedgerTransactionRepository transactions;
    @Mock private BalanceCalculator calculator;
    @Mock private OrganizationQueryPort organizations;
    @Test
    void returnsActiveBusinessAccountSummariesWithoutBankingDetails() {
        LedgerAccount operating = LedgerAccount.create(1L, 42L, ApiEnvironment.LIVE,
                LedgerAccountType.OPERATING, null, null, null, CurrencyCode.NGN, NormalBalance.DEBIT);
        LedgerAccount clearing = LedgerAccount.create(2L, 42L, ApiEnvironment.LIVE,
                LedgerAccountType.PROVIDER_CLEARING, null, null, null, CurrencyCode.NGN, NormalBalance.DEBIT);
        ZonedDateTime now = ZonedDateTime.now();
        when(accounts.findAllByOrganizationIdAndEnvironment(42L, ApiEnvironment.LIVE))
                .thenReturn(List.of(operating, clearing));
        when(accounts.findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
                42L, ApiEnvironment.LIVE, LedgerAccountType.PROVIDER_CLEARING, CurrencyCode.NGN))
                .thenReturn(Optional.of(clearing));
        when(snapshots.findLatestByAccountId(1L)).thenReturn(Optional.of(BalanceSnapshot.create(
                10L, 1L, Money.of(new BigDecimal("9000"), CurrencyCode.NGN), now)));
        when(snapshots.findLatestByAccountId(2L)).thenReturn(Optional.of(BalanceSnapshot.create(
                11L, 2L, Money.of(new BigDecimal("1000"), CurrencyCode.NGN), now)));
        when(transactions.findByAccountIdAndEnvironmentAndPostedAtAfter(1L, ApiEnvironment.LIVE, now))
                .thenReturn(List.of());
        when(transactions.findByAccountIdAndEnvironmentAndPostedAtAfter(2L, ApiEnvironment.LIVE, now))
                .thenReturn(List.of());
        when(calculator.calculateRunningBalance(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("9000"), new BigDecimal("1000"));
        when(calculator.calculateAvailableBalance(
                LedgerAccountType.OPERATING, operating.getStatus(), new BigDecimal("9000"),
                new BigDecimal("1000"))).thenReturn(new BigDecimal("8000"));
        when(organizations.findById(42L)).thenReturn(Optional.of(organization()));
        var result = new ListFinancialAccountsHandler(accounts, snapshots, transactions, calculator, organizations)
                .execute(new ListFinancialAccountsQuery(42L, "LIVE"));

        assertEquals(1, result.size());
        assertEquals("Operating Account", result.getFirst().name());
        assertEquals("AtlasHub Ltd", result.getFirst().businessName());
        assertEquals(new BigDecimal("9000"), result.getFirst().ledgerBalance());
        assertEquals(new BigDecimal("8000"), result.getFirst().availableBalance());
        assertEquals(new BigDecimal("1000"), result.getFirst().pendingSettlementBalance());
        assertNull(result.getFirst().bankAccount());
    }

    private OrganizationQueryPort.OrganizationDto organization() {
        return new OrganizationQueryPort.OrganizationDto(42L, "AtlasHub Ltd", null, null, null,
                null, null, null, "NG", "NGN");
    }
}
