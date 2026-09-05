package com.atlashub.accounts.application.port;

import com.atlashub.accounts.application.result.InternalAccountDto;
import com.atlashub.accounts.application.result.VirtualAccountDto;
import java.util.List;
import java.util.Optional;

public interface AccountQueryService {
    Long getAccountId(Long organizationId, String typeName);
    Long getPlatformAccountId(String typeName);
    Optional<InternalAccountDto> getInternalAccount(Long organizationId, String accountType);
    List<VirtualAccountDto> getVirtualAccounts(Long integrationId);
    long countVirtualAccountsByIntegration(Long integrationId);
    boolean existsVirtualAccountByIntegrationAndBankName(Long integrationId, String bankName);
}
