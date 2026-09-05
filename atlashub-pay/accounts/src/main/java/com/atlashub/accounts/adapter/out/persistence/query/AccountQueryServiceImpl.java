package com.atlashub.accounts.adapter.out.persistence.query;

import com.atlashub.accounts.adapter.out.persistence.repository.SpringDataInternalAccountRepository;
import com.atlashub.accounts.adapter.out.persistence.repository.JpaVirtualAccountRepository;
import com.atlashub.accounts.application.port.AccountQueryService;
import com.atlashub.accounts.application.result.InternalAccountDto;
import com.atlashub.accounts.application.result.VirtualAccountDto;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountQueryServiceImpl implements AccountQueryService {

    private final SpringDataInternalAccountRepository internalRepo;
    private final JpaVirtualAccountRepository virtualRepo;

    @Override
    public Long getAccountId(Long organizationId, String typeName) {
        return internalRepo.findByOrganizationIdAndType(organizationId, typeName)
                .map(entity -> entity.getId())
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Account not found for type: " + typeName));
    }

    @Override
    public Long getPlatformAccountId(String typeName) {
        return internalRepo.findByOrganizationIdAndType(0L, typeName)
                .map(entity -> entity.getId())
                .orElseThrow(() -> new NotFoundException(SharedErrorCode.INVALID_ID, "Platform account not found for type: " + typeName));
    }

    @Override
    public Optional<InternalAccountDto> getInternalAccount(Long organizationId, String accountType) {
        return internalRepo.findByOrganizationIdAndType(organizationId, accountType)
                .map(account -> new InternalAccountDto(
                        account.getId(),
                        account.getOrganizationId(),
                        account.getType(),
                        account.getCurrency(),
                        account.getStatus()
                ));
    }

    @Override
    public List<VirtualAccountDto> getVirtualAccounts(Long integrationId) {
        return virtualRepo.findByIntegration(integrationId).stream()
                .map(account -> new VirtualAccountDto(
                        account.getId(),
                        account.getIntegration(),
                        account.getUserCode(),
                        account.getAccountName(),
                        account.getNuban(),
                        account.getBankName(),
                        account.getStatus()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public long countVirtualAccountsByIntegration(Long integrationId) {
        return virtualRepo.findByIntegration(integrationId).size();
    }

    @Override
    public boolean existsVirtualAccountByIntegrationAndBankName(Long integrationId, String bankName) {
        return virtualRepo.findByIntegration(integrationId).stream()
                .anyMatch(account -> bankName.equals(account.getBankName()));
    }
}
