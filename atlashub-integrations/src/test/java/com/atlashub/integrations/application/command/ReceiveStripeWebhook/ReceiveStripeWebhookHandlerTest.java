package com.atlashub.integrations.application.command.ReceiveStripeWebhook;

import com.atlashub.integrations.application.port.CredentialVaultPort;
import com.atlashub.integrations.application.port.WebhookInbox;
import com.atlashub.integrations.domain.entities.ProviderAuthorization;
import com.atlashub.integrations.domain.entities.ProviderConnection;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import com.atlashub.integrations.infrastructure.external.stripe.adapters.StripeWebhookVerifierAdapter;
import com.atlashub.integrations.support.MemoryConnectionRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReceiveStripeWebhookHandlerTest {
    @Test
    void signedDeauthorizationRevokesAuthorizationAndReplayIsIdempotent() throws Exception {
        var repository = new MemoryConnectionRepository();
        repository.saveConnection(new ProviderConnection(
                1L, 2L, Provider.STRIPE, "US", ProviderConnection.Status.CONNECTED));
        repository.saveAuthorization(new ProviderAuthorization(
                7L, 1L, ApiEnvironment.TEST, "acct_1", ProviderAuthorization.Status.ACTIVE,
                ProviderAuthorization.Health.HEALTHY, Set.of(Capability.CARD_COLLECTION), "vault"));
        Set<String> seen = new HashSet<>();
        WebhookInbox inbox = new WebhookInbox() {
            @Override
            public boolean begin(String provider, Long connectionId, String eventId, byte[] body) {
                return seen.add(eventId);
            }

            @Override
            public void markPublished(String provider, Long connectionId, String eventId) {
            }
        };
        var verifier = new StripeWebhookVerifierAdapter(new ObjectMapper());
        set(verifier, "testSecret", "whsec_test");
        set(verifier, "liveSecret", "whsec_live");
        var handler = new ReceiveStripeWebhookHandler(verifier, repository, inbox, new Vault());
        String payload = "{\"id\":\"evt_1\",\"type\":\"account.application.deauthorized\",\"account\":\"acct_1\"}";
        long timestamp = Instant.now().getEpochSecond();
        String header = "t=" + timestamp + ",v1=" + hmac(timestamp + "." + payload, "whsec_test");
        var command = new ReceiveStripeWebhookCommand(payload.getBytes(StandardCharsets.UTF_8), header);

        handler.execute(command);
        handler.execute(command);

        assertEquals(ProviderAuthorization.Status.REVOKED,
                repository.findAuthorization(7L).orElseThrow().status());
        assertThrows(IllegalArgumentException.class, () -> handler.execute(
                new ReceiveStripeWebhookCommand(payload.getBytes(StandardCharsets.UTF_8),
                        "t=" + timestamp + ",v1=bad")));
    }

    private void set(Object target, String name, String value) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private String hmac(String value, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static final class Vault implements CredentialVaultPort {
        @Override
        public String store(String credential) {
            return credential;
        }

        @Override
        public String read(String reference) {
            return "secret";
        }
    }
}
