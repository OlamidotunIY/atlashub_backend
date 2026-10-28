package com.atlashub.integrations.application.port;

import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.port.ChargeProviderPort;

public interface ConnectedChargeAdapter {
    Provider provider();
    ChargeProviderPort.InitializationResult initialize(ChargeProviderPort.InitializationRequest request,
                                                       String credential);
    ChargeProviderPort.RefundResult refund(ChargeProviderPort.RefundRequest request, String credential);
    ChargeProviderPort.ProviderStatus fetchStatus(String providerReference, String credential);
}
