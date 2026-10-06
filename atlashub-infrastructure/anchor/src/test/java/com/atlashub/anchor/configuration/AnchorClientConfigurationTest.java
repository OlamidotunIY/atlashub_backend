package com.atlashub.anchor.configuration;

import com.atlashub.anchor.client.AnchorClientRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnchorClientConfigurationTest {

    @Test
    void creates_isolated_client_sets_for_sandbox_and_live() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "atlashub.integrations.anchor.enabled=true",
                    "atlashub.integrations.anchor.webhook-publish-timeout=5s",
                    "atlashub.integrations.anchor.sandbox.base-url=https://api.sandbox.getanchor.co",
                    "atlashub.integrations.anchor.sandbox.api-key=sandbox-key",
                    "atlashub.integrations.anchor.sandbox.webhooks.compliance.callback-url=https://sandbox.atlashub.com/api/v1/webhooks/anchor/sandbox/compliance",
                    "atlashub.integrations.anchor.sandbox.webhooks.compliance.token=sandbox-compliance-token",
                    "atlashub.integrations.anchor.sandbox.webhooks.pay-accounts.callback-url=https://sandbox.atlashub.com/api/v1/webhooks/anchor/sandbox/pay-accounts",
                    "atlashub.integrations.anchor.sandbox.webhooks.pay-accounts.token=sandbox-pay-accounts-token",
                    "atlashub.integrations.anchor.sandbox.connect-timeout=2s",
                    "atlashub.integrations.anchor.sandbox.read-timeout=10s",
                    "atlashub.integrations.anchor.live.base-url=https://api.getanchor.co",
                    "atlashub.integrations.anchor.live.api-key=live-key",
                    "atlashub.integrations.anchor.live.webhooks.compliance.callback-url=https://api.atlashub.com/api/v1/webhooks/anchor/live/compliance",
                    "atlashub.integrations.anchor.live.webhooks.compliance.token=live-compliance-token",
                    "atlashub.integrations.anchor.live.webhooks.pay-accounts.callback-url=https://api.atlashub.com/api/v1/webhooks/anchor/live/pay-accounts",
                    "atlashub.integrations.anchor.live.webhooks.pay-accounts.token=live-pay-accounts-token",
                    "atlashub.integrations.anchor.live.connect-timeout=2s",
                    "atlashub.integrations.anchor.live.read-timeout=10s"
            );
            context.register(AnchorClientConfiguration.class, TestRestClientConfiguration.class);
            context.refresh();

            AnchorClientRegistry registry = context.getBean(AnchorClientRegistry.class);
            assertNotNull(registry.forEnvironment(AnchorEnvironment.SANDBOX).depositAccounts());
            assertNotNull(registry.forEnvironment(AnchorEnvironment.LIVE).depositAccounts());
            assertNotNull(registry.forEnvironment(AnchorEnvironment.SANDBOX).subAccounts());
            assertNotNull(registry.forEnvironment(AnchorEnvironment.LIVE).reservedAccounts());
            assertNotNull(registry.forEnvironment(AnchorEnvironment.SANDBOX).webhooks());
            assertNotNull(registry.forEnvironment(AnchorEnvironment.LIVE).businessCustomers());
            assertNotNull(registry.forEnvironment(AnchorEnvironment.LIVE).documents());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestRestClientConfiguration {
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }
}
