package com.atlashub.ledger.application.usecase;

import com.atlashub.ledger.application.command.PostLedgerTransactionCommand;

import com.atlashub.ledger.domain.exception.LedgerErrorCode;
import com.atlashub.ledger.domain.model.*;
import com.atlashub.ledger.domain.repository.BalanceSnapshotRepository;
import com.atlashub.ledger.domain.repository.LedgerEntryRepository;
import com.atlashub.ledger.domain.repository.LedgerTransactionRepository;
import com.atlashub.ledger.domain.valueobject.EntryType;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.ConflictException;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.money.Money;
import com.atlashub.shared.application.port.out.AccountDetailsDto;
import com.atlashub.shared.application.port.out.AccountQueryPort;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostLedgerTransactionUseCase extends BaseUseCase<PostLedgerTransactionCommand, Void> {

    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BalanceSnapshotRepository balanceSnapshotRepository;
    private final AccountQueryPort accountQueryPort;

    public PostLedgerTransactionUseCase(
            LedgerTransactionRepository ledgerTransactionRepository, 
            LedgerEntryRepository ledgerEntryRepository, 
            BalanceSnapshotRepository balanceSnapshotRepository,
            AccountQueryPort accountQueryPort) {
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.accountQueryPort = accountQueryPort;
    }

    @Override
    @Transactional
    public Void execute(PostLedgerTransactionCommand command) {
        // Idempotency constraint using uniqueness check
        if (ledgerTransactionRepository.existsByReference(command.transactionId(), command.sourceSystem())) {
            throw new ConflictException(LedgerErrorCode.TRANSACTION_ALREADY_POSTED, "Transaction already posted");
        }

        TransactionReference reference = new TransactionReference(command.transactionId(), command.sourceSystem());
        
        Long integrationId = command.integrationId();
        if (integrationId == null) {
            throw new BusinessRuleException(LedgerErrorCode.UNAUTHORIZED_ACCESS, "Integration ID is required");
        }

        // Validate entries against account invariants
        Map<Long, AccountDetailsDto> validatedAccounts = new HashMap<>();
        for (PostLedgerTransactionCommand.EntryCommand entry : command.entries()) {
            AccountDetailsDto account = accountQueryPort.findAccountDetails(entry.accountId())
                    .orElseThrow(() -> new NotFoundException(LedgerErrorCode.ACCOUNT_NOT_FOUND, "Account does not exist"));

            if (!account.integration().equals(integrationId)) {
                throw new BusinessRuleException(LedgerErrorCode.UNAUTHORIZED_ACCESS, "Account does not belong to integration");
            }
            if (account.currency() != entry.currency()) {
                throw new BusinessRuleException(LedgerErrorCode.CURRENCY_MISMATCH, "Entry currency does not match account currency");
            }
            validatedAccounts.put(entry.accountId(), account);
        }

        // Lock accounts in a consistent order to prevent deadlocks
        List<Long> accountIds = command.entries().stream()
                .map(PostLedgerTransactionCommand.EntryCommand::accountId)
                .distinct()
                .sorted()
                .toList();

        Map<Long, BalanceSnapshot> snapshots = new HashMap<>();
        for (Long accountId : accountIds) {
            BalanceSnapshot snapshot = balanceSnapshotRepository.findLatestByAccountIdForUpdate(accountId)
                    .orElseGet(() -> new BalanceSnapshot(
                            balanceSnapshotRepository.nextIdentity(),
                            accountId,
                            Money.zero(validatedAccounts.get(accountId).currency()),
                            0L,
                            ZonedDateTime.now()
                    ));
            snapshots.put(accountId, snapshot);
        }

        // Map entries and compute running balances
        List<LedgerEntry> entries = command.entries().stream().map(entry -> {
            BalanceSnapshot snapshot = snapshots.get(entry.accountId());
            Money currentBalance = snapshot.getBalance();
            Money newBalance = entry.type() == EntryType.CREDIT 
                    ? currentBalance.add(Money.of(entry.amount(), entry.currency())) 
                    : currentBalance.subtract(Money.of(entry.amount(), entry.currency()));
            
            Long entryId = ledgerEntryRepository.nextIdentity();
            
            // Update snapshot in memory
            snapshot = snapshot.update(newBalance, entryId);

            return new LedgerEntry(
                entryId,
                entry.accountId(),
                Money.of(entry.amount(), entry.currency()),
                entry.type(),
                reference,
                entry.description(),
                newBalance,
                ZonedDateTime.now()
            );
        }).toList();

        // Instantiate Aggregate Root which enforces Double-Entry balancing constraints
        LedgerTransaction transaction = new LedgerTransaction(ledgerTransactionRepository.nextIdentity(), reference, entries, ZonedDateTime.now());

        ledgerTransactionRepository.save(transaction);
        
        // Persist all updated snapshots
        for (BalanceSnapshot snapshot : snapshots.values()) {
            balanceSnapshotRepository.save(snapshot);
        }

        return null;
    }
}
