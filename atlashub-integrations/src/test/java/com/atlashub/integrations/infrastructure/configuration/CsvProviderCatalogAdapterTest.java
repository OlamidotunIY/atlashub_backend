package com.atlashub.integrations.infrastructure.configuration;

import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvProviderCatalogAdapterTest {
    private final CsvProviderCatalogAdapter catalog = new CsvProviderCatalogAdapter();

    @Test
    void countryCurrencyAndEnvironmentBoundThePublishedOffering() {
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
    void anchorBankingNeverClaimsMerchantCardCollection() {
        assertTrue(catalog.supports(Provider.ANCHOR, "NG", ApiEnvironment.TEST,
                "NGN", Capability.BANKING));
        assertFalse(catalog.supports(Provider.ANCHOR, "NG", ApiEnvironment.TEST,
                "NGN", Capability.CARD_COLLECTION));
    }

    @Test
    void stripeMarketUsesMerchantCountry() {
        assertTrue(catalog.supports(Provider.STRIPE, "FR", ApiEnvironment.LIVE,
                "EUR", Capability.CARD_COLLECTION));
        assertFalse(catalog.supports(Provider.STRIPE, "NG", ApiEnvironment.LIVE,
                "NGN", Capability.CARD_COLLECTION));
    }
}
