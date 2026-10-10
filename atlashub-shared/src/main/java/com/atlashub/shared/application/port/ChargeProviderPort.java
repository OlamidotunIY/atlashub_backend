package com.atlashub.shared.application.port;

import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import java.math.BigDecimal;
import java.util.Map;

public interface ChargeProviderPort {
    InitializationResult initialize(InitializationRequest request);
    RefundResult refund(RefundRequest request);
    ProviderStatus fetchStatus(String providerReference, ApiEnvironment environment);
    record InitializationRequest(ApiEnvironment environment,String email,BigDecimal amount,CurrencyCode currency,
                                 ChargeChannel channel,String reference,String settlementMerchantId,
                                 Map<String,Object> metadata){}
    record InitializationResult(String providerReference,String authorizationUrl,String accessCode){}
    record RefundRequest(ApiEnvironment environment,String providerReference,BigDecimal amount,
                         CurrencyCode currency,String reason,String idempotencyKey){}
    record RefundResult(String refundReference,String status){}
    record ProviderStatus(String providerReference,String status,BigDecimal amount,CurrencyCode currency,String channel,
                          BigDecimal providerFee){
        public ProviderStatus(String providerReference,String status,BigDecimal amount,CurrencyCode currency,
                              String channel){this(providerReference,status,amount,currency,channel,BigDecimal.ZERO);}
    }
    enum ChargeChannel { CARD, USSD }
}
