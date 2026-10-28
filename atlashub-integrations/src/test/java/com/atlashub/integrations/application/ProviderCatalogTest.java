package com.atlashub.integrations.application;

import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProviderCatalogTest {
    private final ProviderCatalog catalog = new ProviderCatalog();

    @Test
    void country_currency_and_environment_bound_the_published_offering() {
        assertTrue(catalog.supports(Provider.PAYSTACK, "NG", ApiEnvironment.LIVE,
                "NGN", Capability.CARD_COLLECTION));
        assertFalse(catalog.supports(Provider.PAYSTACK, "US", ApiEnvironment.LIVE,
                "USD", Capability.CARD_COLLECTION));
        assertFalse(catalog.supports(Provider.PAYSTACK, "NG", ApiEnvironment.LIVE,
                "GHS", Capability.CARD_COLLECTION));
        assertFalse(catalog.supports(Provider.PAYSTACK, "GH", ApiEnvironment.TEST,
                "GHS", Capability.VIRTUAL_ACCOUNT_COLLECTION));
    }

    @Test
    void anchor_banking_never_claims_merchant_card_collection() {
        assertTrue(catalog.supports(Provider.ANCHOR, "NG", ApiEnvironment.TEST,
                "NGN", Capability.BANKING));
        assertFalse(catalog.supports(Provider.ANCHOR, "NG", ApiEnvironment.TEST,
                "NGN", Capability.CARD_COLLECTION));
    }

    @Test
    void stripe_market_is_based_on_the_merchants_country_not_the_buyers_country() {
        assertTrue(catalog.supports(Provider.STRIPE, "FR", ApiEnvironment.LIVE,
                "EUR", Capability.CARD_COLLECTION));
        assertFalse(catalog.supports(Provider.STRIPE, "NG", ApiEnvironment.LIVE,
                "NGN", Capability.CARD_COLLECTION));
    }
}
