package com.atlashub.ledger.application.usecase;

import com.atlashub.ledger.application.command.ProcessWalletChargeCommand;
import com.atlashub.ledger.application.port.out.InternalAccountPort;
import com.atlashub.ledger.domain.event.WalletChargeFailedEvent;
import com.atlashub.ledger.domain.event.WalletChargeSuccessfulEvent;
import com.atlashub.ledger.domain.model.LedgerEntry;
import com.atlashub.ledger.domain.model.LedgerTransaction;
import com.atlashub.ledger.domain.model.TransactionReference;
import com.atlashub.ledger.domain.repository.LedgerTransactionRepository;
import com.atlashub.ledger.domain.valueobject.EntryType;
import com.atlashub.ledger.domain.valueobject.SourceSystem;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.application.usecase.BaseUseCase;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.atlashub.shared.domain.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProcessWalletChargeUseCase extends BaseUseCase<ProcessWalletChargeCommand, Void> {

    private final InternalAccountPort internalAccountPort;
    private final LedgerTransactionRepository transactionRepository;
    private final DomainEventPublisher publisher;
    private final com.atlashub.shared.adapter.out.external.DomainSequenceGenerator idGenerator;

    // Hardcoded platform revenue account ID for now. In a real app, fetch from system config.
    private static final Long PLATFORM_REVENUE_ACCOUNT_ID = 1L;

    public ProcessWalletChargeUseCase(
            InternalAccountPort internalAccountPort,
            LedgerTransactionRepository transactionRepository,
            DomainEventPublisher publisher,
            com.atlashub.shared.adapter.out.external.DomainSequenceGenerator idGenerator) {
        this.internalAccountPort = internalAccountPort;
        this.transactionRepository = transactionRepository;
        this.publisher = publisher;
        this.idGenerator = idGenerator;
    }

    @Override
    @Transactional
    public Void execute(ProcessWalletChargeCommand command) {
        String eventId = UUID.randomUUID().toString();
        ZonedDateTime occurredAt = ZonedDateTime.now();
        
        try {
            Long operatingAccountId = internalAccountPort.getAccountId(command.organizationId(), "OPERATING");

            TransactionReference ref = new TransactionReference(String.valueOf(command.invoiceId()), SourceSystem.SUBSCRIPTIONS);
            Money chargeAmount = Money.of(command.amount(), CurrencyCode.valueOf(command.currency()));

            List<LedgerEntry> entries = new ArrayList<>();
            // Debit Organization's Operating Account
            entries.add(new LedgerEntry(
                    idGenerator.nextIdentity("ledger_entry_seq"),
                    operatingAccountId,
                    chargeAmount,
                    EntryType.DEBIT,
                    ref,
                    "Payment for invoice " + command.invoiceId(),
                    null, // running balance simplified
                    occurredAt
            ));

            // Credit Platform Revenue Account
            entries.add(new LedgerEntry(
                    idGenerator.nextIdentity("ledger_entry_seq"),
                    PLATFORM_REVENUE_ACCOUNT_ID,
                    chargeAmount,
                    EntryType.CREDIT,
                    ref,
                    "Payment for invoice " + command.invoiceId(),
                    null, // running balance simplified
                    occurredAt
            ));

            LedgerTransaction transaction = new LedgerTransaction(
                    idGenerator.nextIdentity("ledger_txn_seq"),
                    ref,
                    entries,
                    occurredAt
            );
            
            LedgerTransaction saved = transactionRepository.save(transaction);
            publishEvents(saved, publisher); // Publish LedgerTransactionPostedEvent

            // Publish Success Event for Billing to consume
            WalletChargeSuccessfulEvent successEvent = new WalletChargeSuccessfulEvent(
                    eventId,
                    String.valueOf(command.invoiceId()),
                    occurredAt,
                    new WalletChargeSuccessfulEvent.Payload(command.invoiceId(), String.valueOf(saved.getId()))
            );
            publisher.publish(EnvelopedDomainEvent.wrap(successEvent));

        } catch (Exception e) {
            // Publish Failure Event
            WalletChargeFailedEvent failureEvent = new WalletChargeFailedEvent(
                    eventId,
                    String.valueOf(command.invoiceId()),
                    occurredAt,
                    new WalletChargeFailedEvent.Payload(command.invoiceId(), e.getMessage())
            );
            publisher.publish(EnvelopedDomainEvent.wrap(failureEvent));
        }

        return null;
    }
}