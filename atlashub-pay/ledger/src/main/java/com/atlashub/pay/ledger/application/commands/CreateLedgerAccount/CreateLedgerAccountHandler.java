package com.atlashub.pay.ledger.application.commands.CreateLedgerAccount;

import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CreateLedgerAccountHandler extends Command<CreateLedgerAccountCommand, CreateLedgerAccountResponse> {

    private static final Logger log = LoggerFactory.getLogger(CreateLedgerAccountHandler.class);
    private final LedgerAccountRepository ledgerAccountRepository;

    public CreateLedgerAccountHandler(LedgerAccountRepository ledgerAccountRepository) {
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Override
    public CreateLedgerAccountResponse execute(CreateLedgerAccountCommand command) {
        log.info("Executing CreateLedgerAccountCommand for organizationId: {}", command.organizationId());

        LedgerAccountType accountType = LedgerAccountType.valueOf(command.accountType().toUpperCase());
        
        if (accountType == LedgerAccountType.TILL) {
            if (command.outletId() == null) {
                throw new BusinessRuleException("Outlet ID is required for TILL accounts.");
            }
            if (ledgerAccountRepository.findByOrganizationIdAndOutletId(command.organizationId(), command.outletId()).isPresent()) {
                throw new BusinessRuleException("A TILL account already exists for this outlet.");
            }
        } else {
            if (ledgerAccountRepository.findByOrganizationIdAndAccountType(command.organizationId(), accountType).isPresent()) {
                throw new BusinessRuleException("A " + accountType + " account already exists for this organization.");
            }
        }

        CurrencyCode currency = CurrencyCode.valueOf(command.currency().toUpperCase());
        Long newId = ledgerAccountRepository.nextIdentity();

        LedgerAccount account = LedgerAccount.create(
                newId,
                command.organizationId(),
                accountType,
                command.outletId(),
                currency
        );

        ledgerAccountRepository.save(account);

        return new CreateLedgerAccountResponse(account.getId());
    }
}
