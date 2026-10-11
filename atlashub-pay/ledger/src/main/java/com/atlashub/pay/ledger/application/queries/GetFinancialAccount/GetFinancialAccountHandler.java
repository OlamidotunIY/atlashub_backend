package com.atlashub.pay.ledger.application.queries.GetFinancialAccount;

import com.atlashub.pay.ledger.application.queries.FinancialAccountResults.BankAccountResult;
import com.atlashub.pay.ledger.application.queries.FinancialAccountResults.FinancialAccountResult;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
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
import java.util.stream.Collectors;

@Component
public class GetFinancialAccountHandler extends Query<GetFinancialAccountQuery, FinancialAccountResult> {
    private final LedgerAccountRepository accounts;
    private final BalanceSnapshotRepository snapshots;
    private final LedgerTransactionRepository transactions;
    private final BalanceCalculator calculator;
    private final BusinessBankingQueryPort banking;

    public GetFinancialAccountHandler(LedgerAccountRepository accounts, BalanceSnapshotRepository snapshots,
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
    public FinancialAccountResult execute(GetFinancialAccountQuery query) {
        ApiEnvironment environment = ApiEnvironment.parse(query.environment());
        LedgerAccount account = accounts.findById(query.accountId())
                .filter(candidate -> candidate.getOrganizationId().equals(query.organizationId()))
                .filter(candidate -> candidate.getEnvironment() == environment)
                .filter(candidate -> candidate.scope() == LedgerAccountScope.BUSINESS)
                .filter(candidate -> candidate.getStatus() == LedgerAccountStatus.ACTIVE)
                .orElseThrow(() -> new LedgerAccountNotFoundException(query.accountId().toString()));
        var snapshot = snapshots.findLatestByAccountId(account.getId())
                .orElseThrow(() -> new LedgerInvariantException("Missing balance snapshot for account"));
        BigDecimal balance = calculator.calculateRunningBalance(account.getId(), account.getNormalBalance(),
                snapshot.getBalance().amount(), transactions.findByAccountIdAndEnvironmentAndPostedAtAfter(
                        account.getId(), environment, snapshot.getSnapshotAt()));
        BankAccountResult bankAccount = account.getAccountType() == LedgerAccountType.OPERATING
                ? banking.findOperatingAccount(query.organizationId(), environment)
                .map(value -> new BankAccountResult(value.accountId(), value.accountName(), value.maskedAccountNumber(),
                        value.bankName(), value.bankCode(), value.currency(), value.status(), value.activatedAt()))
                .orElse(null)
                : null;
        return new FinancialAccountResult(account.getId(), account.getAccountName(), account.getAccountType().name(),
                account.scope().name(), account.getCurrency().name(), account.getStatus().name(),
                account.getActiveRestrictions().stream().map(Enum::name).collect(Collectors.toSet()), balance,
                ZonedDateTime.now(), bankAccount, account.getCreatedAt());
    }
}
