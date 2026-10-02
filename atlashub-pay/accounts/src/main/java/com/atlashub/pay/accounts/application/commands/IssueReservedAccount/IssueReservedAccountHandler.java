package com.atlashub.pay.accounts.application.commands.IssueReservedAccount;

import com.atlashub.pay.accounts.domain.entities.BusinessSubAccount;
import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.entities.ReservedAccount;
import com.atlashub.pay.accounts.domain.repositories.BusinessSubAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.valueobject.BankingProfileStatus;
import com.atlashub.pay.accounts.domain.valueobject.ExternalAccountStatus;
import com.atlashub.pay.accounts.domain.valueobject.ReservedAccountOwnerType;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class IssueReservedAccountHandler extends Command<IssueReservedAccountCommand, Long> {
    private final OrganizationBankingProfileRepository profileRepository;
    private final BusinessSubAccountRepository subAccountRepository;
    private final ReservedAccountRepository reservedAccountRepository;
    private final BankingProviderRequestRepository providerRequestRepository;

    public IssueReservedAccountHandler(OrganizationBankingProfileRepository profileRepository,
                                       BusinessSubAccountRepository subAccountRepository,
                                       ReservedAccountRepository reservedAccountRepository,
                                       BankingProviderRequestRepository providerRequestRepository) {
        this.profileRepository = profileRepository;
        this.subAccountRepository = subAccountRepository;
        this.reservedAccountRepository = reservedAccountRepository;
        this.providerRequestRepository = providerRequestRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:accounts:create')")
    public Long execute(IssueReservedAccountCommand command) {
        ReservedAccountOwnerType ownerType = ReservedAccountOwnerType.valueOf(command.ownerType().toUpperCase());
        ReservedAccount existing = reservedAccountRepository.findByRequestReference(command.idempotencyKey())
                .orElse(null);
        if (existing != null) return existing.getId();
        if (reservedAccountRepository.findActiveByOwner(
                command.organizationId(), ownerType, command.ownerReferenceId(), command.provider()).isPresent()) {
            throw new IllegalStateException("An active reserved account already exists for this owner and provider");
        }
        OrganizationBankingProfile profile = profileRepository.findByOrganizationId(command.organizationId())
                .orElseThrow(() -> new IllegalStateException("Organization banking is not provisioned"));
        if (profile.getStatus() != BankingProfileStatus.ACTIVE || !profile.getActiveRestrictions().isEmpty()) {
            throw new IllegalStateException("Organization banking is not active");
        }
        BusinessSubAccount subAccount = subAccountRepository.findById(profile.getBusinessSubAccountId())
                .filter(value -> value.getStatus() == ExternalAccountStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Organization subaccount is not active"));
        ReservedAccount account = ReservedAccount.request(
                reservedAccountRepository.nextIdentity(), command.organizationId(), ownerType,
                command.ownerReferenceId(), subAccount.getId(), subAccount.getAnchorSubAccountId(),
                command.provider(), command.idempotencyKey(), CurrencyCode.NGN);
        reservedAccountRepository.save(account);
        providerRequestRepository.save(BankingProviderRequest.create(
                providerRequestRepository.nextIdentity(), BankingProviderRequest.RequestType.RESERVED_ACCOUNT,
                account.getId(), command.idempotencyKey(), command.apiEnvironment(), null, subAccount.getAnchorSubAccountId(),
                command.provider(), command.customer().type(), command.customer().referenceId(),
                command.customer().fullName(), command.customer().email(), command.customer().bvn()));
        return account.getId();
    }
}
