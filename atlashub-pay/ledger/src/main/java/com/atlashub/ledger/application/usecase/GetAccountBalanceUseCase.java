package com.atlashub.ledger.application.usecase;

import com.atlashub.ledger.application.query.GetAccountBalanceQuery;
import com.atlashub.ledger.domain.model.BalanceSnapshot;
import com.atlashub.ledger.domain.valueobject.EntryType;
import com.atlashub.ledger.domain.model.LedgerEntry;
import com.atlashub.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlashub.ledger.domain.repository.LedgerEntryRepository;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.money.Money;
import com.atlashub.accounts.application.port.in.AccountQueryPort;
import com.atlashub.accounts.application.port.in.AccountDetailsDto;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.ledger.domain.exception.LedgerErrorCode;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GetAccountBalanceUseCase extends BaseUseCase<GetAccountBalanceQuery, Money> {

    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountQueryPort accountQueryPort;

    public GetAccountBalanceUseCase(BalanceSnapshotRepository balanceSnapshotRepository, LedgerEntryRepository ledgerEntryRepository, AccountQueryPort accountQueryPort) {
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountQueryPort = accountQueryPort;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Money execute(GetAccountBalanceQuery query) {
        // Fetch O(1) Snapshot
        Optional<BalanceSnapshot> snapshotOpt = balanceSnapshotRepository.findLatestByAccountId(query.accountId());

        // Fetch Account Details and Validate Ownership
        AccountDetailsDto account = accountQueryPort.findAccountDetails(query.accountId())
                .orElseThrow(() -> new NotFoundException(LedgerErrorCode.ACCOUNT_NOT_FOUND, "Account does not exist"));

        if (!account.integration().equals(query.integration())) {
            throw new BusinessRuleException(LedgerErrorCode.UNAUTHORIZED_ACCESS, "Account does not belong to integration");
        }

        if (snapshotOpt.isPresent()) {
            BalanceSnapshot snapshot = snapshotOpt.get();
            return snapshot.getBalance();
        } else {
            return Money.zero(account.currency());
        }
    }
}
