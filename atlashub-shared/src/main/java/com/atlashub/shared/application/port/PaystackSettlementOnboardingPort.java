package com.atlashub.shared.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;

public interface PaystackSettlementOnboardingPort {
    Result configure(Request request);
    record Request(Long organizationId,ApiEnvironment environment,String accountNumber,String bankCode,
                   String accountName,String existingSubaccountCode,String idempotencyReference){}
    record Result(String subaccountCode,String resolvedAccountName,String settlementBankCode,boolean active){}
}
