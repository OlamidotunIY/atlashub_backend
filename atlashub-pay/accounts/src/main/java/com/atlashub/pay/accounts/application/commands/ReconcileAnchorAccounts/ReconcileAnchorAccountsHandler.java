package com.atlashub.pay.accounts.application.commands.ReconcileAnchorAccounts;

import com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus.ApplyAnchorAccountStatusCommand;
import com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus.ApplyAnchorAccountStatusHandler;
import com.atlashub.pay.accounts.domain.ports.AnchorBankingPort;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessSubAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ReconcileAnchorAccountsHandler extends Command<ReconcileAnchorAccountsCommand, Void> {
    private static final Logger log = LoggerFactory.getLogger(ReconcileAnchorAccountsHandler.class);
    private final BusinessDepositAccountRepository depositRepository;
    private final BusinessSubAccountRepository subAccountRepository;
    private final ReservedAccountRepository reservedRepository;
    private final AnchorBankingPort anchor;
    private final ApplyAnchorAccountStatusHandler applyStatus;

    public ReconcileAnchorAccountsHandler(BusinessDepositAccountRepository depositRepository,
                                          BusinessSubAccountRepository subAccountRepository,
                                          ReservedAccountRepository reservedRepository,
                                          AnchorBankingPort anchor,
                                          ApplyAnchorAccountStatusHandler applyStatus) {
        this.depositRepository = depositRepository;
        this.subAccountRepository = subAccountRepository;
        this.reservedRepository = reservedRepository;
        this.anchor = anchor;
        this.applyStatus = applyStatus;
    }

    @Override
    public Void execute(ReconcileAnchorAccountsCommand command) {
        depositRepository.findPendingReconciliation(command.batchSize()).stream()
                .filter(account -> account.getAnchorAccountId() != null)
                .forEach(account -> safely(() -> apply("DEPOSIT_ACCOUNT", account.getEnvironment().name(),
                        anchor.fetchDepositAccount(account.getAnchorAccountId(), account.getEnvironment().name())),
                        "deposit", account.getId()));
        subAccountRepository.findPendingReconciliation(command.batchSize()).stream()
                .filter(account -> account.getAnchorSubAccountId() != null)
                .forEach(account -> safely(() -> apply("SUB_ACCOUNT", account.getEnvironment().name(),
                        anchor.fetchSubAccount(account.getAnchorSubAccountId(), account.getEnvironment().name())),
                        "subaccount", account.getId()));
        reservedRepository.findPendingReconciliation(command.batchSize()).stream()
                .filter(account -> account.getAnchorReservedAccountId() != null)
                .forEach(account -> safely(() -> apply("RESERVED_ACCOUNT", account.getEnvironment().name(),
                        anchor.fetchReservedAccount(account.getAnchorReservedAccountId(), account.getEnvironment().name())),
                        "reserved account", account.getId()));
        return null;
    }

    private void apply(String resourceType, String environment, AnchorBankingPort.AccountDetails details) {
        String status = details.status();
        if (!"ACTIVE".equalsIgnoreCase(status) && !"FAILED".equalsIgnoreCase(status)) return;
        ConfirmedBankingDetails bankingDetails = "FAILED".equalsIgnoreCase(status) ? null
                : new ConfirmedBankingDetails(details.accountName(), details.accountNumber(),
                details.maskedAccountNumber(), details.bankName(), details.bankCode());
        applyStatus.execute(new ApplyAnchorAccountStatusCommand(resourceType, environment,
                details.resourceId(), status, bankingDetails,
                "FAILED".equalsIgnoreCase(status) ? "Anchor reported provisioning failure" : null));
    }

    private void safely(Runnable action, String resourceType, Long localId) {
        try {
            action.run();
        } catch (RuntimeException error) {
            log.warn("Anchor reconciliation failed for {} {}: {}", resourceType, localId, error.getMessage());
        }
    }
}
