package com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent;

import com.atlashub.pay.ledger.application.commands.CreateLedgerAccount.CreateLedgerAccountCommand;
import com.atlashub.pay.ledger.application.commands.CreateLedgerAccount.CreateLedgerAccountHandler;
import com.atlashub.pay.ledger.application.commands.PostLedgerTransaction.PostLedgerTransactionCommand;
import com.atlashub.pay.ledger.application.commands.PostLedgerTransaction.PostLedgerTransactionHandler;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.LedgerRestrictionType;
import com.atlashub.pay.ledger.domain.valueobject.LedgerPartyType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessLedgerEventHandler extends Command<ProcessLedgerEventCommand, Void> {
    private static final List<LedgerAccountType> ORGANIZATION_ACCOUNT_TYPES = List.of(
            LedgerAccountType.OPERATING,
            LedgerAccountType.PAYROLL_RESERVE,
            LedgerAccountType.TAX_HOLDING,
            LedgerAccountType.ESCROW,
            LedgerAccountType.SUSPENSE,
            LedgerAccountType.PROVIDER_CLEARING,
            LedgerAccountType.SPLIT_HOLDING);

    private final CreateLedgerAccountHandler createAccountHandler;
    private final PostLedgerTransactionHandler postTransactionHandler;
    private final LedgerAccountRepository accountRepository;

    public ProcessLedgerEventHandler(CreateLedgerAccountHandler createAccountHandler,
                                     PostLedgerTransactionHandler postTransactionHandler,
                                     LedgerAccountRepository accountRepository) {
        this.createAccountHandler = createAccountHandler;
        this.postTransactionHandler = postTransactionHandler;
        this.accountRepository = accountRepository;
    }

    @Override
    public Void execute(ProcessLedgerEventCommand command) {
        switch (command.action()) {
            case BOOTSTRAP_ORGANIZATION -> ORGANIZATION_ACCOUNT_TYPES.forEach(type ->
                    createIfMissing(command.organizationId(), command.environment(), type,
                            null, null, null, command.currency()));
            case CREATE_PARTY_ACCOUNT -> createPartyAccount(command);
            case CREATE_TILL_ACCOUNT -> {
                if (command.environment() == null) {
                    for (ApiEnvironment environment : ApiEnvironment.values()) {
                        createIfMissing(command.organizationId(), environment.name(), LedgerAccountType.TILL,
                                command.outletId(), null, null, command.currency());
                    }
                } else {
                    createIfMissing(command.organizationId(), command.environment(), LedgerAccountType.TILL,
                            command.outletId(), null, null, command.currency());
                }
            }
            case CHARGE_RECEIVED -> post(
                    command, LedgerAccountType.PROVIDER_CLEARING, LedgerAccountType.OPERATING, null, null);
            case PROVIDER_SETTLED -> post(
                    command, LedgerAccountType.SUSPENSE, LedgerAccountType.PROVIDER_CLEARING, null, null);
            case ORGANIZATION_ACCOUNT_FUNDED -> post(
                    command, LedgerAccountType.SUSPENSE, LedgerAccountType.OPERATING, null, null);
            case RESERVED_ACCOUNT_FUNDED -> postReservedFunding(command);
            case PAYOUT_COMPLETED -> post(
                    command,
                    "PAYROLL".equalsIgnoreCase(command.sourceSystem())
                            ? LedgerAccountType.PAYROLL_RESERVE : LedgerAccountType.OPERATING,
                    LedgerAccountType.SUSPENSE, null, null);
            case TILL_OPENED -> post(
                    command, LedgerAccountType.TILL, LedgerAccountType.OPERATING,
                    command.outletId(), null);
            case TILL_CLOSED -> post(
                    command, LedgerAccountType.OPERATING, LedgerAccountType.TILL,
                    null, command.outletId());
            case RESTRICT_ORGANIZATION -> restrictOrganization(command, true);
            case RELEASE_ORGANIZATION -> restrictOrganization(command, false);
        }
        return null;
    }

    private void restrictOrganization(ProcessLedgerEventCommand command, boolean add) {
        LedgerRestrictionType restriction = LedgerRestrictionType.valueOf(command.partyType());
        accountRepository.findAllByOrganizationId(command.organizationId()).forEach(account -> {
            if (add) {
                account.freeze(restriction);
            } else {
                account.unfreeze(restriction);
            }
            accountRepository.save(account);
        });
    }

    private void createPartyAccount(ProcessLedgerEventCommand command) {
        LedgerAccountType type = partyAccountType(command.partyType());
        createIfMissing(command.organizationId(), command.environment(), type, null, command.partyType(),
                command.partyReferenceId(), command.currency());
    }

    private void postReservedFunding(ProcessLedgerEventCommand command) {
        createPartyAccount(command);
        LedgerAccountType partyAccount = partyAccountType(command.partyType());
        post(command, LedgerAccountType.SUSPENSE, partyAccount, null, null);
    }

    private void createIfMissing(Long organizationId, String apiEnvironment, LedgerAccountType type, Long outletId,
                                 String partyType, String partyReferenceId, String currency) {
        ApiEnvironment environment = ApiEnvironment.parse(apiEnvironment);
        CurrencyCode currencyCode = CurrencyCode.valueOf(currency.toUpperCase());
        boolean exists = type == LedgerAccountType.TILL
                ? accountRepository.findByOrganizationIdAndEnvironmentAndOutletIdAndCurrency(
                        organizationId, environment, outletId, currencyCode).isPresent()
                : type == LedgerAccountType.CUSTOMER_FUNDS || type == LedgerAccountType.VENDOR_PAYABLE
                ? accountRepository.findByOrganizationIdAndEnvironmentAndParty(
                        organizationId, environment, LedgerPartyType.valueOf(partyType.toUpperCase()),
                        partyReferenceId, type, currencyCode).isPresent()
                : accountRepository.findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
                        organizationId, environment, type, currencyCode).isPresent();
        if (!exists) {
            createAccountHandler.execute(new CreateLedgerAccountCommand(
                    organizationId, environment.name(), type.name(), outletId, partyType, partyReferenceId,
                    currency, normalBalance(type)));
        }
    }

    private void post(ProcessLedgerEventCommand command, LedgerAccountType debitType,
                      LedgerAccountType creditType, Long debitOutletId, Long creditOutletId) {
        LedgerAccount debit = resolve(command, debitType, debitOutletId);
        LedgerAccount credit = resolve(command, creditType, creditOutletId);
        postTransactionHandler.execute(new PostLedgerTransactionCommand(
                command.organizationId(), command.environment(), command.reference(), command.sourceSystem(),
                command.sourceReferenceId(), command.action().name(), command.currency(),
                List.of(
                        new PostLedgerTransactionCommand.LedgerEntryRequest(
                                debit.getId(), "DEBIT", command.amount()),
                        new PostLedgerTransactionCommand.LedgerEntryRequest(
                                credit.getId(), "CREDIT", command.amount()))));
    }

    private LedgerAccount resolve(ProcessLedgerEventCommand command, LedgerAccountType type, Long outletId) {
        CurrencyCode currency = CurrencyCode.valueOf(command.currency().toUpperCase());
        if (type == LedgerAccountType.TILL) {
            return accountRepository.findByOrganizationIdAndEnvironmentAndOutletIdAndCurrency(
                            command.organizationId(), ApiEnvironment.parse(command.environment()), outletId, currency)
                    .orElseThrow(() -> new IllegalStateException("TILL ledger account not found"));
        }
        if (type == LedgerAccountType.CUSTOMER_FUNDS || type == LedgerAccountType.VENDOR_PAYABLE) {
            return accountRepository.findByOrganizationIdAndEnvironmentAndParty(
                            command.organizationId(), ApiEnvironment.parse(command.environment()),
                            LedgerPartyType.valueOf(command.partyType().toUpperCase()),
                            command.partyReferenceId(), type, currency)
                    .orElseThrow(() -> new IllegalStateException("Party ledger account not found"));
        }
        return accountRepository.findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
                        command.organizationId(), ApiEnvironment.parse(command.environment()), type, currency)
                .orElseThrow(() -> new IllegalStateException(type + " ledger account not found"));
    }

    private LedgerAccountType partyAccountType(String partyType) {
        return switch (LedgerPartyType.valueOf(partyType.toUpperCase())) {
            case CUSTOMER -> LedgerAccountType.CUSTOMER_FUNDS;
            case VENDOR -> LedgerAccountType.VENDOR_PAYABLE;
        };
    }

    private String normalBalance(LedgerAccountType type) {
        return type == LedgerAccountType.SUSPENSE || type == LedgerAccountType.TILL
                || type == LedgerAccountType.PROVIDER_CLEARING
                ? "DEBIT" : "CREDIT";
    }
}
