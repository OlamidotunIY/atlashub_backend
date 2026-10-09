package com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.BusinessSubAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessSubAccountRepository;
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
public class ApplyAnchorAccountStatusHandler extends Command<ApplyAnchorAccountStatusCommand, Void> {
    private final BusinessDepositAccountRepository depositRepository;
    private final BusinessSubAccountRepository subAccountRepository;
    private final ReservedAccountRepository reservedRepository;
    private final OrganizationBankingProfileRepository profileRepository;
    private final BankingProviderRequestRepository providerRequestRepository;

    public ApplyAnchorAccountStatusHandler(BusinessDepositAccountRepository depositRepository,
            BusinessSubAccountRepository subAccountRepository, ReservedAccountRepository reservedRepository,
            OrganizationBankingProfileRepository profileRepository,
            BankingProviderRequestRepository providerRequestRepository) {
        this.depositRepository = depositRepository;
        this.subAccountRepository = subAccountRepository;
        this.reservedRepository = reservedRepository;
        this.profileRepository = profileRepository;
        this.providerRequestRepository = providerRequestRepository;
    }

    @Override
    @Transactional
    public Void execute(ApplyAnchorAccountStatusCommand command) {
        switch (command.resourceType().toUpperCase()) {
            case "DEPOSIT_ACCOUNT" -> applyDeposit(command);
            case "SUB_ACCOUNT" -> applySubAccount(command);
            case "RESERVED_ACCOUNT" -> applyReserved(command);
            default -> throw new IllegalArgumentException("Unsupported Anchor resource type");
        }
        return null;
    }

    private void applyDeposit(ApplyAnchorAccountStatusCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        BusinessDepositAccount deposit = depositRepository.findByAnchorAccountIdAndEnvironment(command.anchorResourceId(), environment)
                .orElseThrow(() -> new IllegalArgumentException("Deposit account not found"));
        OrganizationBankingProfile profile = profileRepository.findById(deposit.getBankingProfileId()).orElseThrow();
        if ("FAILED".equalsIgnoreCase(command.status())) {
            deposit.fail(command.failureReason());
            depositRepository.save(deposit);
            profile.fail("DEPOSIT_ACCOUNT_FAILED", command.failureReason());
            profileRepository.save(profile);
            return;
        }
        if (deposit.getStatus() == ExternalAccountStatus.FROZEN
                && !profile.getActiveRestrictions().isEmpty()) return;
        deposit.activate(command.details());
        depositRepository.save(deposit);
        if (profile.getActiveRestrictions().contains(BankingRestrictionType.COMPLIANCE)) {
            String freezeReference = "org-banking-freeze-" + deposit.getOrganizationId() + "-"
                    + environment.name().toLowerCase() + "-activation";
            if (providerRequestRepository.findByRequestReferenceAndApiEnvironment(
                    freezeReference, environment.name()).isEmpty()) {
                providerRequestRepository.save(BankingProviderRequest.createDepositLifecycle(
                        providerRequestRepository.nextIdentity(), RequestType.FREEZE_DEPOSIT,
                        deposit.getId(), freezeReference, environment.name(), "Compliance suspended"));
            }
        }
        profile.activate();
        profileRepository.save(profile);
    }

    private void applySubAccount(ApplyAnchorAccountStatusCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        BusinessSubAccount subAccount = subAccountRepository.findByAnchorSubAccountIdAndEnvironment(command.anchorResourceId(), environment)
                .orElseThrow(() -> new IllegalArgumentException("Subaccount not found"));
        if ("FAILED".equalsIgnoreCase(command.status())) {
            subAccount.fail(command.failureReason());
            subAccountRepository.save(subAccount);
            OrganizationBankingProfile profile = profileRepository.findById(subAccount.getBankingProfileId()).orElseThrow();
            profile.markPartiallyProvisioned("SUB_ACCOUNT_FAILED", command.failureReason());
            profileRepository.save(profile);
            return;
        }
        subAccount.activate(command.details());
        subAccountRepository.save(subAccount);
        OrganizationBankingProfile profile = profileRepository.findById(subAccount.getBankingProfileId()).orElseThrow();
        profile.activate();
        profileRepository.save(profile);
    }

    private void applyReserved(ApplyAnchorAccountStatusCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        ReservedAccount account = reservedRepository.findByAnchorReservedAccountIdAndEnvironment(command.anchorResourceId(), environment)
                .orElseThrow(() -> new IllegalArgumentException("Reserved account not found"));
        if ("FAILED".equalsIgnoreCase(command.status())) account.fail(command.failureReason());
        else account.activate(command.details());
        reservedRepository.save(account);
    }
}
