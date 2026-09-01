package com.atlashub.accounts.application.port;

import java.util.Optional;
import com.atlashub.shared.domain.valueobject.NUBAN;

public interface VirtualAccountQueryService {
    int countByIntegration(Long integration);
    boolean existsByIntegrationAndBankName(Long integration, String bankName);
    Optional<String> findIntegrationByNuban(NUBAN nuban);
}
