package com.atlashub.providers.routing;

import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.SharedErrorCode;
import com.atlashub.shared.domain.money.CurrencyCode;
import org.springframework.stereotype.Component;

@Component
public class RoutingPolicy {

    public String resolveProviderBeanName(PaymentCapability capability, CurrencyCode currency) {
        if (capability == PaymentCapability.CHARGE) {
            if (currency == CurrencyCode.NGN) {
                return "paystackGatewayAdapter";
            }
        } else if (capability == PaymentCapability.ACCOUNT_ISSUANCE) {
            if (currency == CurrencyCode.NGN) {
                return "anchorVirtualAccountAdapter";
            }
        }
        throw new BusinessRuleException(SharedErrorCode.VALIDATION_ERROR, 
            "No configured provider for capability " + capability + " and currency " + currency);
    }
}
