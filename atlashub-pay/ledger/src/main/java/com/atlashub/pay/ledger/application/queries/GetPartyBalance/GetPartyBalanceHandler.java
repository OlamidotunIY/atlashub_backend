package com.atlashub.pay.ledger.application.queries.GetPartyBalance;

import com.atlashub.pay.ledger.application.queries.GetAccountBalance.AccountBalanceResult;
import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.LedgerPartyType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Component
public class GetPartyBalanceHandler extends Query<GetPartyBalanceQuery, AccountBalanceResult> {
    private final LedgerAccountRepository accountRepository;
    private final BalanceSnapshotRepository snapshotRepository;
    private final LedgerTransactionRepository transactionRepository;
    private final BalanceCalculator calculator;

    public GetPartyBalanceHandler(LedgerAccountRepository accountRepository,
                                  BalanceSnapshotRepository snapshotRepository,
                                  LedgerTransactionRepository transactionRepository,
                                  BalanceCalculator calculator) {
        this.accountRepository = accountRepository;
        this.snapshotRepository = snapshotRepository;
        this.transactionRepository = transactionRepository;
        this.calculator = calculator;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:read')")
    public AccountBalanceResult execute(GetPartyBalanceQuery query) {
        ApiEnvironment environment = ApiEnvironment.parse(query.environment());
        LedgerPartyType partyType = LedgerPartyType.valueOf(query.partyType().toUpperCase());
        LedgerAccountType accountType = partyType == LedgerPartyType.CUSTOMER
                ? LedgerAccountType.CUSTOMER_FUNDS : LedgerAccountType.VENDOR_PAYABLE;
        LedgerAccount account = accountRepository.findByOrganizationIdAndEnvironmentAndParty(
                        query.organizationId(), environment, partyType, query.partyReferenceId(), accountType,
                        CurrencyCode.valueOf(query.currency().toUpperCase()))
                .orElseThrow(() -> new LedgerAccountNotFoundException("Party ledger account not found"));
        BalanceSnapshot snapshot = snapshotRepository.findLatestByAccountId(account.getId())
                .orElseThrow(() -> new IllegalStateException("Account missing initial balance snapshot"));
        BigDecimal balance = calculator.calculateRunningBalance(account.getId(), account.getNormalBalance(),
                snapshot.getBalance().amount(), transactionRepository.findByAccountIdAndEnvironmentAndPostedAtAfter(
                        account.getId(), environment, snapshot.getSnapshotAt()));
        return new AccountBalanceResult(account.getId(), accountType, balance,
                account.getCurrency().name(), ZonedDateTime.now());
    }
}
