package com.atlashub.pay.accounts.infrastructure.external;

import com.atlashub.anchor.client.AnchorClientRegistry;
import com.atlashub.anchor.client.AnchorClients;
import com.atlashub.anchor.client.AnchorDepositAccountClient;
import com.atlashub.anchor.client.AnchorReservedAccountClient;
import com.atlashub.anchor.client.AnchorSubAccountClient;
import com.atlashub.anchor.client.AnchorWebhookClient;
import com.atlashub.anchor.client.AnchorBusinessCustomerClient;
import com.atlashub.anchor.client.AnchorDocumentClient;
import com.atlashub.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.configuration.AnchorProperties;
import com.atlashub.anchor.dto.common.AnchorRequest;
import com.atlashub.anchor.dto.common.AnchorResponse;
import com.atlashub.anchor.dto.deposit.CreateDepositAccountData;
import com.atlashub.anchor.dto.deposit.DepositAccountResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnchorBankingAdapterTest {

    @Test
    void delegates_deposit_account_creation_to_the_shared_sandbox_client() {
        AnchorClientRegistry registry = mock(AnchorClientRegistry.class);
        AnchorDepositAccountClient depositAccounts = mock(AnchorDepositAccountClient.class);
        AnchorClients clients = new AnchorClients(
                depositAccounts,
                mock(AnchorSubAccountClient.class),
                mock(AnchorReservedAccountClient.class),
                mock(AnchorBusinessCustomerClient.class),
                mock(AnchorDocumentClient.class),
                mock(AnchorWebhookClient.class)
        );
        ObjectProvider<AnchorClientRegistry> provider = provider(registry);
        when(registry.forEnvironment(AnchorEnvironment.SANDBOX)).thenReturn(clients);
        when(depositAccounts.createDepositAccount(eq("atlas-reference"), any())).thenReturn(new AnchorResponse<>(
                new DepositAccountResource("deposit-1", "DepositAccount",
                        new DepositAccountResource.Attributes(null, null, "AtlasHub", false, "NGN", "1234567890", "CURRENT", "PENDING"),
                        null)
        ));
        AnchorBankingAdapter adapter = new AnchorBankingAdapter(provider, provider(properties()));

        var result = adapter.createBusinessDepositAccount("customer-1", "CURRENT", "atlas-reference", "TEST");

        assertEquals("deposit-1", result.anchorAccountId());
        assertEquals("PENDING", result.status());
        org.mockito.ArgumentCaptor<AnchorRequest<CreateDepositAccountData>> request = org.mockito.ArgumentCaptor.forClass(AnchorRequest.class);
        verify(depositAccounts).createDepositAccount(eq("atlas-reference"), request.capture());
        assertEquals("customer-1", request.getValue().data().relationships().customer().data().id());
        assertEquals("BusinessCustomer", request.getValue().data().relationships().customer().data().type());
    }

    @Test
    void fails_explicitly_when_anchor_is_not_configured() {
        AnchorBankingAdapter adapter = new AnchorBankingAdapter(provider(null), provider(properties()));

        assertThrows(IllegalStateException.class,
                () -> adapter.createBusinessDepositAccount("customer-1", "CURRENT", "atlas-reference", "TEST"));
    }

    private <T> ObjectProvider<T> provider(T value) {
        @SuppressWarnings("unchecked")
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    private AnchorProperties properties() {
        AnchorProperties.WebhookSubscriptions subscriptions = new AnchorProperties.WebhookSubscriptions(
                new AnchorProperties.WebhookSubscriptionProperties(URI.create("https://api.atlashub.test/anchor/compliance"), "token"),
                new AnchorProperties.WebhookSubscriptionProperties(URI.create("https://api.atlashub.test/anchor/pay-accounts"), "token"));
        AnchorProperties.ProgrammeCapabilities capabilities =
                new AnchorProperties.ProgrammeCapabilities(true, true, true, true, "fbo-account");
        AnchorProperties.EnvironmentProperties environment = new AnchorProperties.EnvironmentProperties(
                URI.create("https://api.sandbox.getanchor.co"), "key", subscriptions, capabilities,
                Duration.ofSeconds(2), Duration.ofSeconds(10));
        return new AnchorProperties(Duration.ofSeconds(5), environment, environment);
    }
}
