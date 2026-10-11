package com.atlashub.pay.accounts.application.commands.RecordAnchorDebit;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingStateException;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecordAnchorDebitHandler extends Command<RecordAnchorDebitCommand, Void> {
    private final BusinessDepositAccountRepository repository;

    public RecordAnchorDebitHandler(BusinessDepositAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Void execute(RecordAnchorDebitCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        BusinessDepositAccount account = repository
                .findByAnchorAccountIdAndEnvironment(command.depositAccountId(), environment)
                .orElseThrow(() -> new InvalidBankingStateException(
                        "Organization debit source account was not found"));
        CurrencyCode currency = CurrencyCode.valueOf(command.currency().toUpperCase());
        if (command.reversed()) {
            account.recordDebitReversal(command.anchorTransferId(), command.transferReference(), command.amount(),
                    currency, command.narration(), command.occurredAt());
        } else {
            account.recordDebit(command.anchorTransferId(), command.transferReference(), command.amount(), currency,
                    command.recipientName(), mask(command.recipientAccountNumber()), command.recipientBankCode(),
                    command.narration(), command.occurredAt());
        }
        repository.save(account);
        return null;
    }

    private String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) return null;
        int visible = Math.min(4, accountNumber.length());
        return "*".repeat(accountNumber.length() - visible)
                + accountNumber.substring(accountNumber.length() - visible);
    }
}
