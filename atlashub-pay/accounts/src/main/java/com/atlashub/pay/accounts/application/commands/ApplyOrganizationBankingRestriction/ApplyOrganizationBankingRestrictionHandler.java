package com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.valueobject.BankingRestrictionType;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.RequestType;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApplyOrganizationBankingRestrictionHandler
        extends Command<ApplyOrganizationBankingRestrictionCommand, Void> {
    private final OrganizationBankingProfileRepository profileRepository;
    private final BusinessDepositAccountRepository depositRepository;
    private final ReservedAccountRepository reservedRepository;
    private final BankingProviderRequestRepository requestRepository;

    public ApplyOrganizationBankingRestrictionHandler(
            OrganizationBankingProfileRepository profileRepository,
            BusinessDepositAccountRepository depositRepository,
            ReservedAccountRepository reservedRepository,
            BankingProviderRequestRepository requestRepository) {
        this.profileRepository = profileRepository;
        this.depositRepository = depositRepository;
        this.reservedRepository = reservedRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional
    public Void execute(ApplyOrganizationBankingRestrictionCommand command) {
        for (ApiEnvironment environment : ApiEnvironment.values()) {
            profileRepository.findByOrganizationIdAndEnvironment(command.organizationId(), environment)
                    .ifPresent(profile -> apply(command, environment, profile));
        }
        return null;
    }

    private void apply(ApplyOrganizationBankingRestrictionCommand command, ApiEnvironment environment,
                       OrganizationBankingProfile profile) {
        BankingRestrictionType restriction = BankingRestrictionType.valueOf(command.restrictionType().toUpperCase());
        if (command.restricted()) profile.restrict(restriction);
        else profile.removeRestriction(restriction);

        for (ReservedAccount account : reservedRepository.findByOrganizationIdAndEnvironment(
                command.organizationId(), environment)) {
            if (command.restricted()) account.restrict(restriction);
            else account.removeRestriction(restriction);
            reservedRepository.save(account);
        }

        BusinessDepositAccount deposit = depositRepository
                .findByOrganizationIdAndEnvironment(command.organizationId(), environment).orElse(null);
        if (deposit != null && deposit.getAnchorAccountId() != null) {
            if (command.restricted() && deposit.getStatus() == ExternalAccountStatus.ACTIVE) {
                enqueueDepositLifecycle(command, environment, profile, deposit);
            } else if (!command.restricted() && profile.getActiveRestrictions().isEmpty()
                    && deposit.getStatus() == ExternalAccountStatus.FROZEN) {
                enqueueDepositLifecycle(command, environment, profile, deposit);
            } else if (!command.restricted() && profile.getActiveRestrictions().isEmpty()) {
                profile.reactivate();
            }
        } else if (!command.restricted() && profile.getActiveRestrictions().isEmpty()) {
            profile.reactivate();
        }
        profileRepository.save(profile);
    }

    private void enqueueDepositLifecycle(ApplyOrganizationBankingRestrictionCommand command,
                                         ApiEnvironment environment,
                                         OrganizationBankingProfile profile,
                                         BusinessDepositAccount account) {
        if (!command.restricted() && !profile.getActiveRestrictions().isEmpty()) return;
        RequestType type = command.restricted()
                ? RequestType.FREEZE_DEPOSIT
                : RequestType.UNFREEZE_DEPOSIT;
        String action = command.restricted() ? "freeze" : "unfreeze";
        String reference = "org-banking-" + action + "-" + command.organizationId() + "-"
                + environment.name().toLowerCase() + "-" + command.operationId();
        if (requestRepository.findByRequestReferenceAndApiEnvironment(reference, environment.name()).isPresent()) return;
        requestRepository.save(BankingProviderRequest.createDepositLifecycle(
                requestRepository.nextIdentity(), type, account.getId(), reference,
                environment.name(), command.reason()));
    }
}
