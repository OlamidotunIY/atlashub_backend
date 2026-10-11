package com.atlashub.pay.ledger.application.queries.GetFinancialAccount;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.NormalBalance;
import com.atlashub.shared.application.port.BusinessBankingQueryPort;
import com.atlashub.shared.application.port.OrganizationQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFinancialAccountHandlerTest {
    @Mock private LedgerAccountRepository accounts;
    @Mock private BalanceSnapshotRepository snapshots;
    @Mock private LedgerTransactionRepository transactions;
    @Mock private BalanceCalculator calculator;
    @Mock private BusinessBankingQueryPort banking;
    @Mock private OrganizationQueryPort organizations;

    @Test
    void includesActiveBankAccountOnlyForIndividualOperatingAccount() {
        LedgerAccount operating = LedgerAccount.create(1L, 42L, ApiEnvironment.LIVE,
                LedgerAccountType.OPERATING, null, null, null, CurrencyCode.NGN, NormalBalance.DEBIT);
        ZonedDateTime now = ZonedDateTime.now();
        when(accounts.findById(1L)).thenReturn(Optional.of(operating));
        LedgerAccount clearing = LedgerAccount.create(2L, 42L, ApiEnvironment.LIVE,
                LedgerAccountType.PROVIDER_CLEARING, null, null, null, CurrencyCode.NGN, NormalBalance.DEBIT);
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
        when(calculator.calculateAvailableBalance(LedgerAccountType.OPERATING, operating.getStatus(),
                new BigDecimal("9000"), new BigDecimal("1000"))).thenReturn(new BigDecimal("8000"));
        when(organizations.findById(42L)).thenReturn(Optional.of(new OrganizationQueryPort.OrganizationDto(
                42L, "AtlasHub Ltd", null, null, null, null, null, null, "NG", "NGN")));
        when(banking.findOperatingAccount(42L, ApiEnvironment.LIVE)).thenReturn(Optional.of(
                new BusinessBankingQueryPort.OperatingBankAccount(50L, "AtlasHub Ltd", "******1234",
                        "Anchor MFB", "090270", "NGN", "ACTIVE", now)));

        var result = new GetFinancialAccountHandler(
                accounts, snapshots, transactions, calculator, banking, organizations)
                .execute(new GetFinancialAccountQuery(42L, "LIVE", 1L));

        assertEquals("Operating Account", result.name());
        assertEquals("AtlasHub Ltd", result.businessName());
        assertEquals(new BigDecimal("9000"), result.ledgerBalance());
        assertEquals(new BigDecimal("8000"), result.availableBalance());
        assertEquals(new BigDecimal("1000"), result.pendingSettlementBalance());
        assertNotNull(result.bankAccount());
        assertEquals("******1234", result.bankAccount().maskedAccountNumber());
    }
}
