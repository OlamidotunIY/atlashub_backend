package com.atlashub.pay.ledger.application.queries.ListFinancialAccounts;

import com.atlashub.pay.ledger.application.queries.FinancialAccountResults.BankAccountResult;
import com.atlashub.pay.ledger.application.queries.FinancialAccountResults.FinancialAccountResult;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.LedgerInvariantException;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountScope;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountStatus;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.shared.application.port.BusinessBankingQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
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
    private final BusinessBankingQueryPort banking;

    public ListFinancialAccountsHandler(LedgerAccountRepository accounts, BalanceSnapshotRepository snapshots,
                                        LedgerTransactionRepository transactions, BalanceCalculator calculator,
                                        BusinessBankingQueryPort banking) {
        this.accounts = accounts;
        this.snapshots = snapshots;
        this.transactions = transactions;
        this.calculator = calculator;
        this.banking = banking;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:read')")
    public List<FinancialAccountResult> execute(ListFinancialAccountsQuery query) {
        ApiEnvironment environment = ApiEnvironment.parse(query.environment());
        BankAccountResult bankAccount = banking.findOperatingAccount(query.organizationId(), environment)
                .map(account -> new BankAccountResult(account.accountId(), account.accountName(),
                        account.maskedAccountNumber(), account.bankName(), account.bankCode(), account.currency(),
                        account.status(), account.activatedAt()))
                .orElse(null);
        return accounts.findAllByOrganizationIdAndEnvironment(query.organizationId(), environment).stream()
                .filter(account -> account.scope() == LedgerAccountScope.BUSINESS
                        && account.getStatus() == LedgerAccountStatus.ACTIVE)
                .map(account -> map(account, environment,
                        account.getAccountType() == LedgerAccountType.OPERATING ? bankAccount : null))
                .toList();
    }

    private FinancialAccountResult map(LedgerAccount account, ApiEnvironment environment,
                                       BankAccountResult bankAccount) {
        var snapshot = snapshots.findLatestByAccountId(account.getId())
                .orElseThrow(() -> new LedgerInvariantException("Missing balance snapshot for account"));
        BigDecimal balance = calculator.calculateRunningBalance(account.getId(), account.getNormalBalance(),
                snapshot.getBalance().amount(), transactions.findByAccountIdAndEnvironmentAndPostedAtAfter(
                        account.getId(), environment, snapshot.getSnapshotAt()));
        return new FinancialAccountResult(account.getId(), account.getAccountName(), account.getAccountType().name(),
                account.scope().name(), account.getCurrency().name(), account.getStatus().name(),
                account.getActiveRestrictions().stream().map(Enum::name).collect(Collectors.toSet()), balance,
                ZonedDateTime.now(), bankAccount, account.getCreatedAt());
    }
}
