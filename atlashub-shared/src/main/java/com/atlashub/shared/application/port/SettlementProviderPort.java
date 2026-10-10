package com.atlashub.shared.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;
import java.util.List;

public interface SettlementProviderPort {
    List<Batch> fetchSuccessfulSettlements(ApiEnvironment environment,String subaccountCode,
                                            String afterProviderSettlementId);
    record Batch(String providerSettlementId,Money grossAmount,Money netAmount,Money providerFeeAmount,
                 ZonedDateTime settledAt,List<String> transactionReferences){}
}
