package com.atlashub.pay.ledger.application.commands.TransferBetweenAccounts;

import com.atlashub.pay.ledger.application.commands.PostLedgerTransaction.PostLedgerTransactionCommand;
import com.atlashub.pay.ledger.application.commands.PostLedgerTransaction.PostLedgerTransactionHandler;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.exceptions.CurrencyMismatchException;
import com.atlashub.pay.ledger.domain.exceptions.InsufficientFundsException;
import com.atlashub.pay.ledger.domain.exceptions.LedgerAccountNotFoundException;
import com.atlashub.pay.ledger.domain.exceptions.LedgerInvariantException;
import com.atlashub.pay.ledger.domain.exceptions.UnsupportedBusinessAccountTypeException;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountScope;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TransferBetweenAccountsHandler
        extends Command<TransferBetweenAccountsCommand, TransferBetweenAccountsResult> {
    private final LedgerAccountRepository accounts;
    private final BalanceSnapshotRepository snapshots;
    private final LedgerTransactionRepository transactions;
    private final BalanceCalculator calculator;
    private final PostLedgerTransactionHandler postHandler;

    public TransferBetweenAccountsHandler(LedgerAccountRepository accounts, BalanceSnapshotRepository snapshots,
                                          LedgerTransactionRepository transactions, BalanceCalculator calculator,
                                          PostLedgerTransactionHandler postHandler) {
        this.accounts = accounts;
        this.snapshots = snapshots;
        this.transactions = transactions;
        this.calculator = calculator;
        this.postHandler = postHandler;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:ledger:manage')")
    public TransferBetweenAccountsResult execute(TransferBetweenAccountsCommand command) {
        validate(command);
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        LedgerAccount source = account(command.sourceAccountId(), command.organizationId(), environment);
        LedgerAccount destination = account(command.destinationAccountId(), command.organizationId(), environment);
        if (source.scope() != LedgerAccountScope.BUSINESS || destination.scope() != LedgerAccountScope.BUSINESS) {
            throw new UnsupportedBusinessAccountTypeException("SYSTEM_OR_PARTY");
        }
        if (source.getCurrency() != destination.getCurrency()
                || !source.getCurrency().name().equalsIgnoreCase(command.currency())) {
            throw new CurrencyMismatchException();
        }
        var snapshot = snapshots.findLatestByAccountId(source.getId())
                .orElseThrow(() -> new LedgerInvariantException("Missing balance snapshot for source account"));
        BigDecimal balance = calculator.calculateRunningBalance(source.getId(), source.getNormalBalance(),
                snapshot.getBalance().amount(), transactions.findByAccountIdAndEnvironmentAndPostedAtAfter(
                        source.getId(), environment, snapshot.getSnapshotAt()));
        if (balance.compareTo(command.amount()) < 0) {
            throw new InsufficientFundsException();
        }
        var result = postHandler.execute(new PostLedgerTransactionCommand(command.organizationId(),
                command.environment(), command.reference(), "INTERNAL_TRANSFER", command.reference(),
                command.description(), command.currency(), List.of(
                new PostLedgerTransactionCommand.LedgerEntryRequest(
                        source.getId(), "DEBIT", command.amount()),
                new PostLedgerTransactionCommand.LedgerEntryRequest(
                        destination.getId(), "CREDIT", command.amount()))));
        return new TransferBetweenAccountsResult(result.transactionId(), result.reference(), result.postedAt());
    }

    private void validate(TransferBetweenAccountsCommand command) {
        if (command.sourceAccountId() == null || command.destinationAccountId() == null
                || command.sourceAccountId().equals(command.destinationAccountId())) {
            throw new LedgerInvariantException("Source and destination must be different accounts");
        }
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new LedgerInvariantException("Transfer amount must be greater than zero");
        }
        if (command.reference() == null || command.reference().isBlank()) {
            throw new LedgerInvariantException("Transfer reference is required");
        }
    }

    private LedgerAccount account(Long id, Long organizationId, ApiEnvironment environment) {
        LedgerAccount account = accounts.findById(id)
                .orElseThrow(() -> new LedgerAccountNotFoundException(id.toString()));
        if (!account.getOrganizationId().equals(organizationId) || account.getEnvironment() != environment) {
            throw new LedgerAccountNotFoundException(id.toString());
        }
        return account;
    }
}
