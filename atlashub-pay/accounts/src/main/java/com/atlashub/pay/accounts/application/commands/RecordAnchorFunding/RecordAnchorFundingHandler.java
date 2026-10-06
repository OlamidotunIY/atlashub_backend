package com.atlashub.pay.accounts.application.commands.RecordAnchorFunding;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingStateException;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecordAnchorFundingHandler extends Command<RecordAnchorFundingCommand, Void> {
    private final ReservedAccountRepository reservedRepository;
    private final BusinessDepositAccountRepository depositRepository;

    public RecordAnchorFundingHandler(ReservedAccountRepository reservedRepository,
                                      BusinessDepositAccountRepository depositRepository) {
        this.reservedRepository = reservedRepository;
        this.depositRepository = depositRepository;
    }

    @Override
    @Transactional
    public Void execute(RecordAnchorFundingCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        CurrencyCode currency = CurrencyCode.valueOf(command.currency().toUpperCase());
        if (command.reservedAccountId() != null) {
            ReservedAccount account = reservedRepository
                    .findByAnchorReservedAccountIdAndEnvironment(command.reservedAccountId(), environment)
                    .orElseThrow(() -> new InvalidBankingStateException("Reserved account funding destination was not found"));
            account.recordFunding(command.transferReference(), command.amount(), currency,
                    command.senderAccountName(), command.senderBankCode(), command.receivedAt());
            reservedRepository.save(account);
            return null;
        }
        if (command.depositAccountId() != null) {
            BusinessDepositAccount account = depositRepository
                    .findByAnchorAccountIdAndEnvironment(command.depositAccountId(), environment)
                    .orElseThrow(() -> new InvalidBankingStateException("Organization funding destination was not found"));
            account.recordFunding(command.transferReference(), command.amount(), currency, command.receivedAt());
            depositRepository.save(account);
            return null;
        }
        throw new InvalidBankingStateException("Anchor funding destination is missing");
    }
}
