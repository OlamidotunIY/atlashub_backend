package com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.stereotype.Component;

@Component
public class ProvisionOrganizationBankingHandler extends Command<ProvisionOrganizationBankingCommand, Void> {
    private final OrganizationBankingProfileRepository profileRepository;
    private final BusinessDepositAccountRepository depositRepository;
    private final BankingProviderRequestRepository providerRequestRepository;

    public ProvisionOrganizationBankingHandler(OrganizationBankingProfileRepository profileRepository,
                                               BusinessDepositAccountRepository depositRepository,
                                               BankingProviderRequestRepository providerRequestRepository) {
        this.profileRepository = profileRepository;
        this.depositRepository = depositRepository;
        this.providerRequestRepository = providerRequestRepository;
    }

    @Override
    public Void execute(ProvisionOrganizationBankingCommand command) {
        if (profileRepository.findByOrganizationId(command.organizationId()).isPresent()) return null;
        OrganizationBankingProfile profile = OrganizationBankingProfile.create(
                profileRepository.nextIdentity(), command.organizationId(), command.anchorBusinessCustomerId());
        profileRepository.save(profile);
        BusinessDepositAccount deposit = BusinessDepositAccount.request(
                depositRepository.nextIdentity(), command.organizationId(), profile.getId(),
                command.anchorBusinessCustomerId(), CurrencyCode.NGN);
        depositRepository.save(deposit);
        profile.linkDepositAccount(deposit.getId());
        profileRepository.save(profile);
        String reference = "org-banking-deposit-" + command.organizationId();
        providerRequestRepository.save(BankingProviderRequest.create(
                providerRequestRepository.nextIdentity(), BankingProviderRequest.RequestType.DEPOSIT,
                deposit.getId(), reference, "LIVE", command.anchorBusinessCustomerId(), null,
                null, null, null, null, null, null));
        return null;
    }
}
