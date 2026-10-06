package com.atlashub.pay.accounts.application.queries.GetBusinessBanking;

import com.atlashub.pay.accounts.application.queries.AccountResults.BusinessBankingResult;
import com.atlashub.pay.accounts.application.queries.AccountResults.ExternalAccountResult;
import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.BusinessSubAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessSubAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import com.atlashub.shared.application.security.ApiEnvironment;

@Component
public class GetBusinessBankingHandler extends Query<GetBusinessBankingQuery, BusinessBankingResult> {
    private final OrganizationBankingProfileRepository profileRepository;
    private final BusinessDepositAccountRepository depositRepository;
    private final BusinessSubAccountRepository subAccountRepository;

    public GetBusinessBankingHandler(OrganizationBankingProfileRepository profileRepository,
            BusinessDepositAccountRepository depositRepository, BusinessSubAccountRepository subAccountRepository) {
        this.profileRepository = profileRepository;
        this.depositRepository = depositRepository;
        this.subAccountRepository = subAccountRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('pay:accounts:read')")
    public BusinessBankingResult execute(GetBusinessBankingQuery query) {
        ApiEnvironment environment = ApiEnvironment.parse(query.environment());
        OrganizationBankingProfile profile = profileRepository.findByOrganizationIdAndEnvironment(query.organizationId(), environment)
                .orElseThrow(() -> new IllegalArgumentException("Banking profile not found"));
        return new BusinessBankingResult(profile.getId(), profile.getStatus().name(),
                depositRepository.findByOrganizationIdAndEnvironment(query.organizationId(), environment).map(this::deposit).orElse(null),
                subAccountRepository.findByOrganizationIdAndEnvironment(query.organizationId(), environment).map(this::subAccount).orElse(null));
    }

    private ExternalAccountResult deposit(BusinessDepositAccount value) {
        return new ExternalAccountResult(value.getId(), value.getAccountName(), value.getMaskedAccountNumber(),
                value.getBankName(), value.getBankCode(), value.getCurrency().name(), value.getStatus().name(),
                value.getActivatedAt());
    }

    private ExternalAccountResult subAccount(BusinessSubAccount value) {
        return new ExternalAccountResult(value.getId(), value.getAccountName(), value.getMaskedAccountNumber(),
                value.getBankName(), value.getBankCode(), value.getCurrency().name(), value.getStatus().name(),
                value.getActivatedAt());
    }
}
