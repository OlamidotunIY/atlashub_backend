package com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.pay.accounts.domain.exceptions.InvalidBankingStateException;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.stereotype.Component;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProvisionOrganizationBankingHandler extends Command<ProvisionOrganizationBankingCommand, Void> {
    private final OrganizationBankingProfileRepository profileRepository;
    private final BusinessDepositAccountRepository depositRepository;
    private final BankingProviderRequestRepository providerRequestRepository;

    public ProvisionOrganizationBankingHandler(OrganizationBankingProfileRepository profileRepository, BusinessDepositAccountRepository depositRepository, BankingProviderRequestRepository providerRequestRepository) {
        this.profileRepository = profileRepository;
        this.depositRepository = depositRepository;
        this.providerRequestRepository = providerRequestRepository;
    }

    @Override
    @Transactional
    public Void execute(ProvisionOrganizationBankingCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        OrganizationBankingProfile profile = profileRepository
                .findByOrganizationIdAndEnvironment(command.organizationId(), environment)
                .orElseGet(() -> profileRepository.save(OrganizationBankingProfile.create(
                        profileRepository.nextIdentity(), command.organizationId(), environment,
                        command.anchorBusinessCustomerId())));
        if (!profile.getAnchorBusinessCustomerId().equals(command.anchorBusinessCustomerId())) {
            throw new InvalidBankingStateException("Banking profile is already linked to a different provider customer");
        }

        BusinessDepositAccount deposit = depositRepository
                .findByOrganizationIdAndEnvironment(command.organizationId(), environment)
                .orElseGet(() -> depositRepository.save(BusinessDepositAccount.request(
                        depositRepository.nextIdentity(), command.organizationId(), environment, profile.getId(),
                        command.anchorBusinessCustomerId(), CurrencyCode.NGN)));
        if (profile.getBusinessDepositAccountId() == null) {
            profile.linkDepositAccount(deposit.getId());
            profileRepository.save(profile);
        }

        String reference = "org-banking-deposit-" + command.organizationId() + "-" + environment.name().toLowerCase();
        if (providerRequestRepository.findByRequestReferenceAndApiEnvironment(reference, environment.name()).isEmpty()) {
            providerRequestRepository.save(BankingProviderRequest.create(providerRequestRepository.nextIdentity(),
                    BankingProviderRequest.RequestType.DEPOSIT, deposit.getId(), reference, environment.name(),
                    command.anchorBusinessCustomerId(), null, null, null, null, null, null, null));
        }
        return null;
    }
}
