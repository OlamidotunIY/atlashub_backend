package com.atlashub.shared.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;
import java.time.ZonedDateTime;
import java.util.Optional;

public interface BusinessBankingQueryPort {
    Optional<OperatingBankAccount> findOperatingAccount(Long organizationId, ApiEnvironment environment);

    record OperatingBankAccount(Long accountId, String accountName, String maskedAccountNumber,
                                String bankName, String bankCode, String currency, String status,
                                ZonedDateTime activatedAt) {
    }
}
