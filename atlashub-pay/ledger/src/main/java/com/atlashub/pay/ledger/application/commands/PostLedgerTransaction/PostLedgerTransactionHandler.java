package com.atlashub.pay.ledger.application.commands.PostLedgerTransaction;

import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.entities.LedgerEntry;
import com.atlashub.pay.ledger.domain.entities.LedgerTransaction;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountClosedException;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountFrozenException;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.valueobject.EntryType;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountStatus;
import com.atlashub.pay.ledger.domain.valueobject.SourceSystem;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PostLedgerTransactionHandler extends Command<PostLedgerTransactionCommand, PostLedgerTransactionResponse> {

    private final LedgerTransactionRepository transactionRepository;
    private final LedgerAccountRepository accountRepository;

    public PostLedgerTransactionHandler(
            LedgerTransactionRepository transactionRepository,
            LedgerAccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public PostLedgerTransactionResponse execute(PostLedgerTransactionCommand command) {
        // 1. Check idempotency
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        Optional<LedgerTransaction> existingTx = transactionRepository.findByReferenceAndEnvironment(command.reference(), environment);
        if (existingTx.isPresent()) {
            LedgerTransaction tx = existingTx.get();
            return new PostLedgerTransactionResponse(tx.getId(), tx.getReference(), tx.getPostedAt());
        }

        // 2. Validate entries list has at least one DEBIT and one CREDIT
        boolean hasDebit = false;
        boolean hasCredit = false;
        for (PostLedgerTransactionCommand.LedgerEntryRequest entry : command.entries()) {
            if ("DEBIT".equalsIgnoreCase(entry.entryType())) {
                hasDebit = true;
            } else if ("CREDIT".equalsIgnoreCase(entry.entryType())) {
                hasCredit = true;
            }
        }
        if (!hasDebit || !hasCredit) {
            throw new IllegalArgumentException("Transaction must have at least one DEBIT and one CREDIT entry");
        }

        // 3. Collect all unique accountIds from entries. Sort them in ascending order.
        List<Long> sortedIds = command.entries().stream()
                .map(PostLedgerTransactionCommand.LedgerEntryRequest::accountId)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 4. Acquire locks
        List<LedgerAccount> accounts = accountRepository.findAllByIdInWithLock(sortedIds);
        Map<Long, LedgerAccount> accountMap = accounts.stream()
                .collect(Collectors.toMap(LedgerAccount::getId, Function.identity()));

        // 5. Verify each account
        for (Long accountId : sortedIds) {
            LedgerAccount account = accountMap.get(accountId);
            if (account == null) {
                throw new LedgerAccountNotFoundException(accountId.toString());
            }
            if (!account.getOrganizationId().equals(command.organizationId())) {
                throw new IllegalArgumentException("Account " + accountId + " does not belong to organization " + command.organizationId());
            }
            if (account.getEnvironment() != environment) {
                throw new IllegalArgumentException("Account " + accountId + " belongs to a different API environment");
            }
            if (account.getStatus() == LedgerAccountStatus.FROZEN) {
                throw new LedgerAccountFrozenException(accountId.toString());
            }
            if (account.getStatus() == LedgerAccountStatus.CLOSED) {
                throw new LedgerAccountClosedException(accountId.toString());
            }
        }

        // 6. Create immutable entries. Balances are derived from snapshots plus entries.
        List<LedgerEntry> ledgerEntries = new ArrayList<>();
        Long txId = transactionRepository.nextIdentity();

        for (PostLedgerTransactionCommand.LedgerEntryRequest req : command.entries()) {
            Long entryId = transactionRepository.nextEntryIdentity();
            
            LedgerEntry entry = LedgerEntry.create(
                    entryId,
                    txId,
                    req.accountId(),
                    EntryType.valueOf(req.entryType().toUpperCase()),
                    new Money(req.amount(), CurrencyCode.valueOf(command.currency()))
            );
            ledgerEntries.add(entry);
        }

        // 7. Construct LedgerTransaction
        LedgerTransaction transaction = LedgerTransaction.create(
                txId,
                command.organizationId(),
                environment,
                ledgerEntries,
                SourceSystem.valueOf(command.sourceSystem()),
                command.sourceReferenceId(),
                command.description(),
                CurrencyCode.valueOf(command.currency()),
                ZonedDateTime.now(),
                command.reference()
        );

        // 8. Save
        transactionRepository.save(transaction);
        return new PostLedgerTransactionResponse(transaction.getId(), transaction.getReference(), transaction.getPostedAt());
    }
}
