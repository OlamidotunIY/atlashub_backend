package com.atlashub.pay.ledger.application.queries.GetWalletBalances;

import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.entities.LedgerEntry;
import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.pay.ledger.domain.valueobject.EntryType;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GetWalletBalancesHandler extends Query<GetWalletBalancesQuery, WalletBalancesResult> {

    private static final Logger log = LoggerFactory.getLogger(GetWalletBalancesHandler.class);

    private final LedgerAccountRepository accountRepository;
    private final BalanceSnapshotRepository snapshotRepository;
    private final LedgerTransactionRepository transactionRepository;
    private final BalanceCalculator balanceCalculator;

    public GetWalletBalancesHandler(LedgerAccountRepository accountRepository,
                                    BalanceSnapshotRepository snapshotRepository,
                                    LedgerTransactionRepository transactionRepository,
                                    BalanceCalculator balanceCalculator) {
        this.accountRepository = accountRepository;
        this.snapshotRepository = snapshotRepository;
        this.transactionRepository = transactionRepository;
        this.balanceCalculator = balanceCalculator;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:read')")
    public WalletBalancesResult execute(GetWalletBalancesQuery query) {
        log.info("Executing GetWalletBalancesQuery for organizationId={}", query.organizationId());

        List<LedgerAccount> accounts = accountRepository.findAllByOrganizationIdAndEnvironment(
                query.organizationId(), ApiEnvironment.parse(query.environment())).stream()
                .filter(account -> account.getOutletId() == null && account.getPartyType() == null)
                .toList();
        
        if (accounts.isEmpty()) {
            return new WalletBalancesResult(query.organizationId(), new HashMap<>(), "NGN");
        }
        
        String currency = accounts.getFirst().getCurrency().name();
        List<Long> accountIds = accounts.stream().map(LedgerAccount::getId).collect(Collectors.toList());
        List<BalanceSnapshot> snapshots = snapshotRepository.findAllLatestByAccountIdIn(accountIds);
        
        Map<Long, BalanceSnapshot> snapshotMap = new HashMap<>();
        for (BalanceSnapshot snapshot : snapshots) {
            snapshotMap.put(snapshot.getAccountId(), snapshot);
        }

        Map<String, BigDecimal> balancesByAccountType = new HashMap<>();

        for (LedgerAccount account : accounts) {
            BalanceSnapshot snapshot = snapshotMap.get(account.getId());
            if (snapshot == null) {
                throw new IllegalStateException("Account missing initial balance snapshot: " + account.getId());
            }
            
            BigDecimal balance = snapshot.getBalance().amount();
            ZonedDateTime snapshotDate = snapshot.getSnapshotAt();

            List<LedgerTransaction> transactions = transactionRepository.findByAccountIdAndEnvironmentAndPostedAtAfter(
                    account.getId(), ApiEnvironment.parse(query.environment()), snapshotDate);

            balance = balanceCalculator.calculateRunningBalance(
                    account.getId(), account.getNormalBalance(), balance, transactions);
            
            String typeName = account.getAccountType().name();
            BigDecimal currentTotal = balancesByAccountType.getOrDefault(typeName, BigDecimal.ZERO);
            balancesByAccountType.put(typeName, currentTotal.add(balance));
        }

        return new WalletBalancesResult(query.organizationId(), balancesByAccountType, currency);
    }
}
