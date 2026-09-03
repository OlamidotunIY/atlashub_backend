package com.atlashub.ledger.application.usecase;

import com.atlashub.ledger.application.command.HandleWalletChargeCommand;
import com.atlashub.ledger.application.command.PostLedgerTransactionCommand;
import com.atlashub.ledger.application.port.out.InternalAccountPort;
import com.atlashub.ledger.domain.model.WalletCharge;
import com.atlashub.ledger.domain.repository.WalletChargeRepository;
import com.atlashub.ledger.domain.valueobject.EntryType;
import com.atlashub.ledger.domain.valueobject.InternalAccountType;
import com.atlashub.ledger.domain.valueobject.SourceSystem;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HandleWalletChargeUseCase extends BaseUseCase<HandleWalletChargeCommand, Void> {

    private final PostLedgerTransactionUseCase postLedgerTransactionUseCase;
    private final InternalAccountPort internalAccountPort;
    private final WalletChargeRepository walletChargeRepository;
    private final DomainEventPublisher publisher;

    public HandleWalletChargeUseCase(
            PostLedgerTransactionUseCase postLedgerTransactionUseCase,
            InternalAccountPort internalAccountPort,
            WalletChargeRepository walletChargeRepository,
            DomainEventPublisher publisher) {
        this.postLedgerTransactionUseCase = postLedgerTransactionUseCase;
        this.internalAccountPort = internalAccountPort;
        this.walletChargeRepository = walletChargeRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(HandleWalletChargeCommand command) {
        WalletCharge charge;
        try {
            Long operatingAccountId = internalAccountPort.getAccountId(command.organizationId(), InternalAccountType.OPERATING);
            Long platformTillAccountId = internalAccountPort.getPlatformAccountId(InternalAccountType.TILL);

            PostLedgerTransactionCommand postCommand = getPostCommand(command, operatingAccountId, platformTillAccountId);

            postLedgerTransactionUseCase.execute(postCommand);

            charge = WalletCharge.create(
                    walletChargeRepository.nextIdentity(),
                    command.invoiceId(),
                    command.organizationId(),
                    command.amount(),
                    command.currency().name(),
                    String.valueOf(command.invoiceId())
            );

        } catch (Exception e) {
            charge = WalletCharge.fail(
                    walletChargeRepository.nextIdentity(),
                    command.invoiceId(),
                    command.organizationId(),
                    command.amount(),
                    command.currency().name(),
                    e.getMessage()
            );
        }

        walletChargeRepository.save(charge);
        publishEvents(charge, publisher);
        return null;
    }

    @NonNull
    private static PostLedgerTransactionCommand getPostCommand(HandleWalletChargeCommand command, Long operatingAccountId, Long platformTillAccountId) {
        List<PostLedgerTransactionCommand.EntryCommand> entries = List.of(
                new PostLedgerTransactionCommand.EntryCommand(
                        operatingAccountId,
                        command.amount(),
                        command.currency(),
                        EntryType.DEBIT,
                        "Invoice payment for INV-" + command.invoiceId()
                ),
                new PostLedgerTransactionCommand.EntryCommand(
                        platformTillAccountId,
                        command.amount(),
                        command.currency(),
                        EntryType.CREDIT,
                        "Invoice payment received for INV-" + command.invoiceId()
                )
        );

        PostLedgerTransactionCommand postCommand = new PostLedgerTransactionCommand(
                String.valueOf(command.invoiceId()),
                SourceSystem.SUBSCRIPTIONS,
                command.invoiceId(),
                entries
        );
        return postCommand;
    }
}
