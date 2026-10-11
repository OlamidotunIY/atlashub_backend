package com.atlashub.pay.ledger.application.queries.ListFinancialAccounts;

import com.atlashub.pay.ledger.application.queries.FinancialAccountResults.FinancialAccountResult;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.LedgerInvariantException;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountScope;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.port.OrganizationQueryPort;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListFinancialAccountsHandler
        extends Query<ListFinancialAccountsQuery, List<FinancialAccountResult>> {
    private final LedgerAccountRepository accounts;
    private final BalanceSnapshotRepository snapshots;
    private final LedgerTransactionRepository transactions;
    private final BalanceCalculator calculator;
    private final OrganizationQueryPort organizations;

    public ListFinancialAccountsHandler(LedgerAccountRepository accounts, BalanceSnapshotRepository snapshots,
                                        LedgerTransactionRepository transactions, BalanceCalculator calculator,
                                        OrganizationQueryPort organizations) {
        this.accounts = accounts;
        this.snapshots = snapshots;
        this.transactions = transactions;
        this.calculator = calculator;
        this.organizations = organizations;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:read')")
    public List<FinancialAccountResult> execute(ListFinancialAccountsQuery query) {
        ApiEnvironment environment = ApiEnvironment.parse(query.environment());
        String businessName = organizations.findById(query.organizationId())
                .map(OrganizationQueryPort.OrganizationDto::businessName)
                .orElseThrow(() -> new LedgerInvariantException("Organization was not found"));
        return accounts.findAllByOrganizationIdAndEnvironment(query.organizationId(), environment).stream()
                .filter(account -> account.scope() == LedgerAccountScope.BUSINESS
                        && account.getStatus() != LedgerAccountStatus.CLOSED)
                .map(account -> map(account, environment, businessName))
                .toList();
    }

    private FinancialAccountResult map(LedgerAccount account, ApiEnvironment environment, String businessName) {
        var snapshot = snapshots.findLatestByAccountId(account.getId())
                .orElseThrow(() -> new LedgerInvariantException("Missing balance snapshot for account"));
        BigDecimal balance = calculator.calculateRunningBalance(account.getId(), account.getNormalBalance(),
                snapshot.getBalance().amount(), transactions.findByAccountIdAndEnvironmentAndPostedAtAfter(
                        account.getId(), environment, snapshot.getSnapshotAt()));
        BigDecimal pending = pendingSettlementBalance(account, environment);
        BigDecimal available = calculator.calculateAvailableBalance(
                account.getAccountType(), account.getStatus(), balance, pending);
        return new FinancialAccountResult(account.getId(), businessName, account.getAccountName(),
                account.getAccountType().name(), account.scope().name(), account.getCurrency().name(),
                account.getStatus().name(), account.getStatus() == LedgerAccountStatus.FROZEN,
                account.getActiveRestrictions().stream().map(Enum::name).collect(Collectors.toSet()), balance,
                available, pending, ZonedDateTime.now(), null, account.getCreatedAt());
    }

    private BigDecimal pendingSettlementBalance(LedgerAccount account, ApiEnvironment environment) {
        if (account.getAccountType() != com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType.OPERATING) {
            return BigDecimal.ZERO;
        }
        return accounts.findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
                        account.getOrganizationId(), environment,
                        com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType.PROVIDER_CLEARING,
                        account.getCurrency())
                .map(clearing -> {
                    var snapshot = snapshots.findLatestByAccountId(clearing.getId())
                            .orElseThrow(() -> new LedgerInvariantException(
                                    "Missing provider-clearing balance snapshot"));
                    return calculator.calculateRunningBalance(clearing.getId(), clearing.getNormalBalance(),
                            snapshot.getBalance().amount(),
                            transactions.findByAccountIdAndEnvironmentAndPostedAtAfter(
                                    clearing.getId(), environment, snapshot.getSnapshotAt())).max(BigDecimal.ZERO);
                })
                .orElse(BigDecimal.ZERO);
    }
}
