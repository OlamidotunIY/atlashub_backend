package com.atlashub.pay.ledger.application.queries.GetAccountBalance;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.entities.LedgerEntry;
import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.pay.ledger.domain.valueobject.EntryType;
import com.atlashub.shared.application.usecase.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class GetAccountBalanceHandler extends Query<GetAccountBalanceQuery, AccountBalanceResult> {

    private static final Logger log = LoggerFactory.getLogger(GetAccountBalanceHandler.class);

    private final LedgerAccountRepository accountRepository;
    private final BalanceSnapshotRepository snapshotRepository;
    private final LedgerTransactionRepository transactionRepository;
    private final BalanceCalculator balanceCalculator;

    public GetAccountBalanceHandler(LedgerAccountRepository accountRepository,
                                    BalanceSnapshotRepository snapshotRepository,
                                    LedgerTransactionRepository transactionRepository,
                                    BalanceCalculator balanceCalculator) {
        this.accountRepository = accountRepository;
        this.snapshotRepository = snapshotRepository;
        this.transactionRepository = transactionRepository;
        this.balanceCalculator = balanceCalculator;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:wallets:read')")
    public AccountBalanceResult execute(GetAccountBalanceQuery query) {
        log.info("Executing GetAccountBalanceQuery for accountId={}", query.accountId());

        LedgerAccount account = accountRepository.findById(query.accountId())
                .orElseThrow(() -> new LedgerAccountNotFoundException("LedgerAccount not found: " + query.accountId()));

        if (!account.getOrganizationId().equals(query.organizationId())) {
            throw new IllegalArgumentException("Account does not belong to the given organization");
        }

        BalanceSnapshot snapshot = snapshotRepository.findLatestByAccountId(account.getId())
                .orElseThrow(() -> new IllegalStateException("Account missing initial balance snapshot"));

        BigDecimal balance = snapshot.getBalance().amount();
        ZonedDateTime snapshotDate = snapshot.getSnapshotAt();

        List<LedgerTransaction> transactions = transactionRepository.findByAccountIdAndPostedAtAfter(account.getId(), snapshotDate);

        balance = balanceCalculator.calculateRunningBalance(account.getId(), balance, transactions);

        return new AccountBalanceResult(
                account.getId(),
                account.getAccountType().name(),
                balance,
                account.getCurrency().name(),
                ZonedDateTime.now()
        );
    }
}
