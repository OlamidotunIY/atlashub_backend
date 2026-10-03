package com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.entities.BusinessSubAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessSubAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.ports.AnchorBankingPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.stereotype.Component;

@Component
public class ApplyAnchorAccountStatusHandler extends Command<ApplyAnchorAccountStatusCommand, Void> {
    private final BusinessDepositAccountRepository depositRepository;
    private final BusinessSubAccountRepository subAccountRepository;
    private final ReservedAccountRepository reservedRepository;
    private final OrganizationBankingProfileRepository profileRepository;
    private final BankingProviderRequestRepository providerRequestRepository;
    private final AnchorBankingPort anchorBankingPort;

    public ApplyAnchorAccountStatusHandler(BusinessDepositAccountRepository depositRepository,
            BusinessSubAccountRepository subAccountRepository, ReservedAccountRepository reservedRepository,
            OrganizationBankingProfileRepository profileRepository,
            BankingProviderRequestRepository providerRequestRepository,
            AnchorBankingPort anchorBankingPort) {
        this.depositRepository = depositRepository;
        this.subAccountRepository = subAccountRepository;
        this.reservedRepository = reservedRepository;
        this.profileRepository = profileRepository;
        this.providerRequestRepository = providerRequestRepository;
        this.anchorBankingPort = anchorBankingPort;
    }

    @Override
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
        if ("FAILED".equalsIgnoreCase(command.status())) {
            deposit.fail(command.failureReason());
            depositRepository.save(deposit);
            return;
        }
        deposit.activate(command.details());
        depositRepository.save(deposit);
        OrganizationBankingProfile profile = profileRepository.findById(deposit.getBankingProfileId()).orElseThrow();
        if (profile.getBusinessSubAccountId() != null) return;
        if (!anchorBankingPort.supports("SUB_ACCOUNT", command.environment())) return;
        String parentFboAccountId = anchorBankingPort.requireFboAccountId(command.environment());
        BusinessSubAccount subAccount = BusinessSubAccount.request(
                subAccountRepository.nextIdentity(), deposit.getOrganizationId(), environment, profile.getId(),
                deposit.getAnchorBusinessCustomerId(), parentFboAccountId, CurrencyCode.NGN);
        subAccountRepository.save(subAccount);
        profile.linkSubAccount(subAccount.getId());
        profileRepository.save(profile);
        String reference = "org-banking-subaccount-" + deposit.getOrganizationId();
        providerRequestRepository.save(BankingProviderRequest.create(
                providerRequestRepository.nextIdentity(), BankingProviderRequest.RequestType.SUB_ACCOUNT,
                subAccount.getId(), reference + "-" + environment.name().toLowerCase(), environment.name(), deposit.getAnchorBusinessCustomerId(), parentFboAccountId,
                null, null, null, null, null, null));
    }

    private void applySubAccount(ApplyAnchorAccountStatusCommand command) {
        ApiEnvironment environment = ApiEnvironment.parse(command.environment());
        BusinessSubAccount subAccount = subAccountRepository.findByAnchorSubAccountIdAndEnvironment(command.anchorResourceId(), environment)
                .orElseThrow(() -> new IllegalArgumentException("Subaccount not found"));
        if ("FAILED".equalsIgnoreCase(command.status())) {
            subAccount.fail(command.failureReason());
            subAccountRepository.save(subAccount);
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
